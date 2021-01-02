object DemoApp extends App {
  println("Starting the producer")
  val twitterProducer = TweetProducer()
  twitterProducer.run()
}
