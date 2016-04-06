name := "led-notifier"
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

mainClass in assembly := Some("LedNotifier")
jarName in assembly := "led-notifier.jar"
assembleArtifact in packageScala := true

val pi4jV = "1.0"

libraryDependencies ++= Seq(
		    "com.pi4j" % "pi4j-core" % pi4jV,
		    "com.pi4j" % "pi4j-service" % pi4jV,
		    "com.pi4j" % "pi4j-gpio-extension" % pi4jV,
		    "com.pi4j" % "pi4j-device" % pi4jV,
        "org.erlang.otp" % "jinterface" % "1.5.6",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
        "ch.qos.logback" %  "logback-classic" % "1.1.6",
        "com.google.guava" % "guava" %  "18.0",
        "com.github.scopt" %% "scopt" % "3.4.0"
)
