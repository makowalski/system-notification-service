package com.showyourtrip.notification.services

import akka.actor.{Actor, ActorRef}
import com.showyourtrip.message.models.{Message, Subscribe, Subscribed}
import com.showyourtrip.notification.models.Notification

class NotificationConsumerActor(storeActor: ActorRef, eventBusPath: String) extends Actor {

  override def preStart = {
    context.actorSelection(eventBusPath) ! Subscribe("system", self)
  }

  def subscribed: Receive = {
    case m: Message => {
      storeActor ! Notification(m.receiverId, m.text, m.date)
    }
  }

  def unsubscribed: Receive = {
    case s: Subscribed => context.become(subscribed)
  }

  def receive = unsubscribed
}
