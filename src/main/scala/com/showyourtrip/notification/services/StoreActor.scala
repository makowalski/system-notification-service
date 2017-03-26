package com.showyourtrip.notification.services

import akka.actor.Actor
import com.showyourtrip.notification.models.Notification

class StoreActor(val insertNotification: (Notification) => Unit) extends Actor {

  def receive = {
    case n: Notification => {
      insertNotification(n)
    }
  }
}
