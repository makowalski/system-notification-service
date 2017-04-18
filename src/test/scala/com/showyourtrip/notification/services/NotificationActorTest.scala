package com.showyourtrip.notification.services

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestActors, TestKit}
import com.showyourtrip.message.models.{Message, Subscribe}
import com.showyourtrip.notification.models.Notification
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class NotificationActorTest extends TestKit(ActorSystem("test-system"))
  with FunSuiteLike
  with ImplicitSender
  with BeforeAndAfterAll {

  override def afterAll {
    shutdown()
  }

  test("should create not existing user") {
    val notificationActor = TestActorRef(new NotificationActor)
    val notification = Notification("userId", "test message", "test date")

    notificationActor ! notification

    assert(notificationActor.underlyingActor.users.contains("userId"))
  }
}