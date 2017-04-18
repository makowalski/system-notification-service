package com.showyourtrip.notification.services

import akka.actor.{Actor, ActorRef, Props}
import com.showyourtrip.notification.models.Notification

import scala.collection.mutable

class NotificationActor extends Actor {

  val users = mutable.HashMap.empty[String, ActorRef]

  def receive = {
    case n: Notification => {
      users.get(n.userId) match {
        case Some(user) => user ! n
        case None => {
          val user = initUser(n.userId)
          user ! n
        }
      }
    }
  }

  def initUser(userId: String): ActorRef = {
    val userActor = context.actorOf(Props[UserActor], s"user-$userId")
    users.put(userId, userActor)
    userActor
  }

}
