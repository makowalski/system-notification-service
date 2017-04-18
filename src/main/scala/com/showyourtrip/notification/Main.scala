package com.showyourtrip.notification

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.showyourtrip.notification.models.Notification
import com.showyourtrip.notification.services.{NotificationActor, ConsumerActor, StoreActor}
import com.typesafe.config.ConfigFactory
import kamon.Kamon

object Main extends App {

  Kamon.start()

  implicit val actorSystem = ActorSystem("system-notification-service")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executor = actorSystem.dispatcher

  val eventBusPath = ConfigFactory.load().getString("event.bus.remote.actor")
  val storeActor = actorSystem.actorOf(Props(classOf[StoreActor], Notification.insertFunction), "store")
  val notificationActor = actorSystem.actorOf(Props[NotificationActor], "notification")
  val consumerActor = actorSystem.actorOf(Props(classOf[ConsumerActor], storeActor, notificationActor, eventBusPath), "consumer")

}
