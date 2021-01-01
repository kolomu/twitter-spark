object DemoApp extends App {
  println("Starting...")
  val twitterProducer = TwitterProducer()
  twitterProducer.run()
}
