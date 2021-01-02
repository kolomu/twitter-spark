name := "twitter-spark"

version := "0.1"

scalaVersion := "2.12.12"

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "org.apache.kafka" %% "kafka" % "2.6.0"
libraryDependencies += "com.danielasfregola" %% "twitter4s" % "7.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"