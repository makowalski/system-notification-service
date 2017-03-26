package com.showyourtrip

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import kamon.Kamon

object Main extends App {

  Kamon.start()

  implicit val actorSystem = ActorSystem("system-notification-service")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executor = actorSystem.dispatcher

}
