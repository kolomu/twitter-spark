import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}

import java.util.Properties

case class TweetProducer() extends LazyLogging {

  val props = new Properties()
  // see: https://kafka.apache.org/documentation/#producerconfigs
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "kafka_serialization.TweetSerializer")
  props.put("group.id", "something")
  props.put("acks", "all") // acknowledgement -> leader will wait for replica to notice the record.
  props.put("compression.type", "snappy") // very fast data compression / decompression, see: https://en.wikipedia.org/wiki/Snappy_(compression)
  props.put("linger.ms", "20") /* The producer groups together any records that arrive in between request transmissions into a single batched request.
  Normally this occurs only under load when records arrive faster than they can be sent out. However in some circumstances the client may want to reduce
  the number of requests even under moderate load. This setting accomplishes this by adding a small amount of artificial delay—that is, rather than
  immediately sending out a record the producer will wait for up to the given delay to allow other records to be sent so that the sends can be batched together. */
  val producer = new KafkaProducer[String, Tweet](props)
  val terms = List("coronavirus")

  val client: TwitterStreamingClient = TwitterStreamingClient()
  val topic = "Twitter-Kafka"

  sys.addShutdownHook({
    logger.info("Closing Producer");
    producer.close();
    logger.info("Finished closing")
  })

  def printTweetText: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet => println(tweet.text)
  }

  def sendTweetToKafka: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet => producer.send(new ProducerRecord[String, Tweet](topic, null, tweet), new Callback() {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        if (exception != null) {
          logger.error("Some error OR something bad happened", exception)
        }
      }
    })
  }

  def run(): Unit = {
    logger.info("Started to collect tweets...")
    client.filterStatuses(tracks = terms)(sendTweetToKafka)
    logger.info("Application End")
  }
}
