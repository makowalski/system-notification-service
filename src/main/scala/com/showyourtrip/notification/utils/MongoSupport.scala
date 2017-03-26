package com.showyourtrip.notification.utils

import com.typesafe.config.ConfigFactory
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{MongoConnection, MongoDriver}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MongoSupport {
  val mongoUri = ConfigFactory.load().getString("mongodb.uri")
  val driver = MongoDriver()
  val parsedUri = Future.fromTry(MongoConnection.parseURI(mongoUri))
  val connection = parsedUri.map(driver.connection)
  val database = for {
    uri <- parsedUri
    conn <- connection
    db <- conn.database(uri.db.getOrElse(""))
  } yield db
}

trait MongoSupport {

  def collection(name: String) = MongoSupport.database.map(_.collection[BSONCollection](name))
}
