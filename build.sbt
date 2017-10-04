name := "akka-persistence-experiment"

version := "1.0"

scalaVersion := "2.12.3"

val akkaVersion = "2.5.6"

libraryDependencies ++= Seq(
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.1.1",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion
)
        