package com.showyourtrip.notification.services

import akka.actor.Actor
import com.showyourtrip.notification.models._

import scala.collection.mutable


class UserActor extends Actor {

  val messages = mutable.Queue.empty[Notification]

  def receive = {
    case n: Notification => {
      messages.enqueue(n)
      if (messages.size > 30) {
        messages.dequeue()
      }
    }
    case Read => {
      sender() ! Notifications(messages.toList)
    }
  }
}
