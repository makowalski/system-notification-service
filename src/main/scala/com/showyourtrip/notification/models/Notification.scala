package com.showyourtrip.notification.models

import com.showyourtrip.notification.utils.MongoSupport
import reactivemongo.bson.{BSONDocumentWriter, Macros}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Notification extends MongoSupport {

  implicit def notificationWriter: BSONDocumentWriter[Notification] = Macros.writer[Notification]

  val insertFunction = (message: Notification) =>
    collection("notifications")
      .flatMap(_.insert(message))
      .onComplete(_ match {
        case Success(r) => // TODO handle success log?
        case Failure(e) => // TODO handle error
      })
}

case class Notification(userId: String, text: String, date: String)
