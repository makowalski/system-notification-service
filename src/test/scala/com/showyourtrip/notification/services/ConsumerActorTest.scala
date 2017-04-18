package com.showyourtrip.notification.services

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestActors, TestKit}
import com.showyourtrip.message.models.{Message, Subscribe}
import com.showyourtrip.notification.models.Notification
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class ConsumerActorTest extends TestKit(ActorSystem("test-system"))
  with FunSuiteLike
  with ImplicitSender
  with BeforeAndAfterAll {

  override def afterAll {
    shutdown()
  }

  test("should subscribe to the system topic after start") {
    val blackHole = system.actorOf(TestActors.blackholeProps)
    system.actorOf(TestActors.forwardActorProps(testActor), "test-event-bus")

    val consumerActor = system.actorOf(Props(classOf[ConsumerActor], blackHole, blackHole, "akka.tcp://test-system@127.0.0.1:3653/user/test-event-bus"))
    expectMsg(Subscribe("system", consumerActor))
  }

  test("should send notification to database when message arrives") {
    val blackHole = system.actorOf(TestActors.blackholeProps)
    val consumerActor = TestActorRef(new ConsumerActor(testActor, blackHole, "test-system/event-bus"))
    consumerActor.underlyingActor.context.become(consumerActor.underlyingActor.subscribed)

    val message = Message("system", "simpleReceiver", "simpleText", "simpleDate")
    consumerActor ! message
    expectMsg(Notification("simpleReceiver", "simpleText", "simpleDate"))
  }

  test("should send notification to user when message arrives") {
    val blackHole = system.actorOf(TestActors.blackholeProps)
    val consumerActor = TestActorRef(new ConsumerActor(blackHole, testActor, "test-system/event-bus"))
    consumerActor.underlyingActor.context.become(consumerActor.underlyingActor.subscribed)

    val message = Message("system", "simpleReceiver", "simpleText", "simpleDate")
    consumerActor ! message
    expectMsg(Notification("simpleReceiver", "simpleText", "simpleDate"))
  }
}