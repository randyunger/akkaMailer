name := "sc-email"

organization := "com.ungersoft"

version := "1.1"

description := "Connector for Akka to talk to CouchDB"

resolvers ++= Seq(
	"Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
	"com.typesafe.akka" % "akka-actor" % "2.0.1",
	"com.typesafe.akka" % "akka-file-mailbox" % "2.0.1",
	"com.weiglewilczek.slf4s" % "slf4s_2.9.1" % "1.0.7",
	"org.specs2" %% "specs2" % "1.8.2" % "test",
	"com.typesafe" % "config" % "0.4.0",
	"javax.mail" % "mail" % "1.4"
)