import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}

import java.util
import java.util.Properties
import scala.jdk.CollectionConverters.iterableAsScalaIterableConverter

object TweetConsumer extends LazyLogging with App {
  val props = new Properties()
  // see: https://kafka.apache.org/documentation/#producerconfigs
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("group.id", "something")
  props.put("enable.auto.commit", "true")
  props.put("auto.commit.interval.ms", "1000")
  props.put("session.timeout.ms", "30000")
  props.put("value.deserializer", "kafka_serialization.TweetDeserializer")

  val topic = "Twitter-Kafka"
  val consumer = new KafkaConsumer[String, Tweet](props)
  consumer.subscribe(util.Collections.singletonList(topic))

  def consume(): Unit = {
    while(true) {
      val records: ConsumerRecords[String, Tweet] = consumer.poll(100)
      records.asScala.foreach { record =>
        println(s"Received: ${record.value()} ::: At Partition ::: ${record.partition()} ::: At offset: ${record.offset()}")
      }
    }
  }

  consume()

}
