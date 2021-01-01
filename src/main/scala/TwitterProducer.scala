import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.{Constants, HttpHosts}
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.httpclient.auth.OAuth1
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}

import java.util.Properties
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue, TimeUnit}
import scala.jdk.CollectionConverters.seqAsJavaListConverter

case class TwitterProducer() extends LazyLogging {

  val props = new Properties()
  // see: https://kafka.apache.org/documentation/#producerconfigs
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("group.id", "something")
  props.put("acks", "all") // acknowledgement -> leader will wait for replica to notice the record.
  props.put("compression.type", "snappy") // very fast data compression / decompression, see: https://en.wikipedia.org/wiki/Snappy_(compression)
  props.put("linger.ms", "20") /* The producer groups together any records that arrive in between request transmissions into a single batched request. Normally this occurs only under load when records arrive faster than they can be sent out. However in some circumstances the client may want to reduce the number of requests even under moderate load. This setting accomplishes this by adding a small amount of artificial delay—that is, rather than immediately sending out a record the producer will wait for up to the given delay to allow other records to be sent so that the sends can be batched together.*/
  val producer = new KafkaProducer[String, String](props)
  val terms = List("coronavirus")

  val msgQueue = new LinkedBlockingQueue[String](30)
  val client = createTwitterClient(msgQueue, terms)
  val topic = "Twitter-Kafka"

  sys.addShutdownHook({
    logger.info("Application is not stopping!")
    client.stop();
    logger.info("Closing Producer");
    producer.close();
    logger.info("Finished closing")
  })

  def run(): Unit = {
     // send Tweets to Kafka
    while(!client.isDone) {
      var msg: String = null;
      try {
        msg = msgQueue.poll(5, TimeUnit.SECONDS)
      } catch {
        case e: InterruptedException =>
          e.printStackTrace()
          client.stop()
      }

      if(msg != null) {
        logger.info(msg);
        producer.send(new ProducerRecord[String, String](topic, null, msg), new Callback(){
          def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
            if (e != null) logger.error("Some error OR something bad happened", e)
          }
        })
      }

    }
    logger.info("Application End")
  }

  def createTwitterClient(msgQueue: BlockingQueue[String], terms: List[String]) = {

    // See: https://github.com/twitter/hbc
    val hbEndpoint = new StatusesFilterEndpoint
    hbEndpoint.trackTerms(terms.asJava)
    // Twitter API and tokens
    val hosebirdAuth = new OAuth1(
      sys.env("TWITTER_CONSUMER_TOKEN_KEY"),
      sys.env("TWITTER_CONSUMER_TOKEN_SECRET"),
      sys.env("TWITTER_ACCESS_TOKEN_KEY"),
      sys.env("TWITTER_ACCESS_TOKEN_SECRET"))

    /** Creating a client */
    val builder = new ClientBuilder()
      .name("Hosebird-Client")
      .hosts(new HttpHosts(Constants.STREAM_HOST))
      .authentication(hosebirdAuth)
      .endpoint(hbEndpoint)
      .processor(new StringDelimitedProcessor(msgQueue))
    val hbClient = builder.build
    hbClient
  }
}
