package com.showyourtrip.notification.services

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.showyourtrip.notification.models.{Notification, Notifications, Read}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}


class UserActorTest extends TestKit(ActorSystem("test-system"))
  with FunSuiteLike
  with ImplicitSender
  with BeforeAndAfterAll {

  override def afterAll {
    shutdown()
  }

  test("should store notifications in list") {

    val userActor = TestActorRef(new UserActor)
    val notification = Notification("userId", "test message", "test date")

    userActor ! notification

    assert(userActor.underlyingActor.messages.size == 1)
  }

  test("should store not more than last 30 notifications") {

    val userActor = TestActorRef(new UserActor)

    for (i <- 1 to 31) {
      val notification = Notification("userId", s"test message $i", "test date")
      userActor ! notification
    }

    assert(userActor.underlyingActor.messages.size == 30)
    assert(userActor.underlyingActor.messages.contains(Notification("userId", s"test message 31", "test date")))
    assert(!userActor.underlyingActor.messages.contains(Notification("userId", s"test message 1", "test date")))
  }

  test("should return all notifications") {

    val userActor = TestActorRef(new UserActor)
    val notification1 = Notification("userId", "test message 1", "test date")
    val notification2 = Notification("userId", "test message 2", "test date")
    val notification3 = Notification("userId", "test message 3", "test date")

    userActor ! notification1
    userActor ! notification2
    userActor ! notification3

    userActor ! Read
    assert(userActor.underlyingActor.messages.size == 3)
    expectMsg(Notifications(List(notification1, notification2, notification3)))
  }
}
