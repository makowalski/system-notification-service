package com.showyourtrip.notification.services

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestActors, TestKit}
import com.showyourtrip.message.models.{Message, Subscribe}
import com.showyourtrip.notification.models.Notification
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class NotificationConsumerActorTest extends TestKit(ActorSystem("test-system"))
  with FunSuiteLike
  with ImplicitSender
  with BeforeAndAfterAll {

  override def afterAll {
    shutdown()
  }

  test("should subscribe to the system topic after start") {
    val storeActor = system.actorOf(TestActors.blackholeProps)
    system.actorOf(TestActors.forwardActorProps(testActor), "test-event-bus")

    val notificationConsumerActor = system.actorOf(Props(classOf[NotificationConsumerActor], storeActor, "akka.tcp://test-system@127.0.0.1:3653/user/test-event-bus"))
    expectMsg(Subscribe("system", notificationConsumerActor))
  }

  test("should send notification when message arrives") {
    val notificationConsumerActor = TestActorRef(new NotificationConsumerActor(testActor, "test-system/event-bus"))
    notificationConsumerActor.underlyingActor.context.become(notificationConsumerActor.underlyingActor.subscribed)

    val message = Message("system", "simpleReceiver", "simpleText", "simpleDate")
    notificationConsumerActor ! message
    expectMsg(Notification("simpleReceiver", "simpleText", "simpleDate"))
  }
}