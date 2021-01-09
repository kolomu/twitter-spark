import SparkTweetAnalyzer.spark
import com.danielasfregola.twitter4s.entities.Tweet
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.util.Date

object SparkTweetAnalyzer extends LazyLogging with App {
  val topic = "Twitter-Kafka"

  val spark: SparkSession = SparkSession.builder.appName("TwitterSpark").config("spark.master", "local").getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")
  val streamingContext = new StreamingContext(spark.sparkContext, Seconds(1))
  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "localhost:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> "kafka_serialization.TweetDeserializer",
    "group.id" -> "something"
  )
  val topics = Array(topic)
  val stream = KafkaUtils.createDirectStream[String, Tweet](
    streamingContext,
    PreferConsistent,
    Subscribe[String, Tweet](topics, kafkaParams)
  )

  logger.info(s"Subscribed to topic: $topic")

  val columns = Seq("sentiment", "id", "text")


  val mappedValues: DStream[(Double, Tweet, Date)] = stream
    .filter(rdd => rdd.value().lang.get == "en")
    .map(rdd => {
      val tweet = rdd.value()
      (SentimentAnalyzer.getSentiment(tweet.text), tweet, Date.from(tweet.created_at))
    })

  mappedValues.foreachRDD { rdd =>
    //      rdd.foreach( rc => print("******* - LANG****** : " + rc._2.lang))
    //      // get the Singleton instance of SparkSession
    //      val sparkSingleton = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
    //
    // Convert RDD to DataFrame
    import spark.implicits._
    // FIXME: CANNOT HAVE circular reference in class. But got but got the circular reference of class com.danielasfregola.twitter4s.entities.GeoPlace
    val sentimentDF = rdd.toDF(columns: _*)
    //
    //      // Create a temporary view
    //      sentimentDF.createOrReplaceTempView("sentiment")
    //
    //      // Do Spark SQL stuff
    sentimentDF.show()
  }

  streamingContext.start
  streamingContext.awaitTermination

}
