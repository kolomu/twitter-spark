object DemoApp extends App {
  println("Starting the producer")
  // val value = SentimentAnalyzer.getSentiment("I was me")
  // println(value);

  val twitterProducer = TweetProducer()
  twitterProducer.run()
}
