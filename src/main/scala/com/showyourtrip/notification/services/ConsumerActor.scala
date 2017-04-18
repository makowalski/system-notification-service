package com.showyourtrip.notification.services

import akka.actor.{Actor, ActorRef}
import com.showyourtrip.message.models.{Message, Subscribe, Subscribed}
import com.showyourtrip.notification.models.Notification

class ConsumerActor(storeActor: ActorRef, notificationActor: ActorRef, eventBusPath: String) extends Actor {

  override def preStart = {
    context.actorSelection(eventBusPath) ! Subscribe("system", self)
  }

  def subscribed: Receive = {
    case m: Message => {
      val notification = Notification(m.receiverId, m.text, m.date)
      storeActor ! notification
      notificationActor ! notification
    }
  }

  def unsubscribed: Receive = {
    case s: Subscribed => context.become(subscribed)
  }

  def receive = unsubscribed
}
