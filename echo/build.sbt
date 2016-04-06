name := "echo"
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

mainClass in assembly := Some("Echo")
jarName in assembly := "echo.jar"
assembleArtifact in packageScala := true

libraryDependencies ++= Seq(
        "org.erlang.otp" % "jinterface" % "1.5.6",
        "com.github.scopt" %% "scopt" % "3.4.0"
)
