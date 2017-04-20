package com.showyourtrip.notification

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import com.showyourtrip.message.models.{Message, Subscribe, Subscribed}
import com.showyourtrip.notification.models.{Notification, Notifications, Read}
import com.showyourtrip.notification.services.{ConsumerActor, NotificationActor, StoreActor}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

import scala.concurrent.Future

class IntegrationTest extends TestKit(ActorSystem("test-system"))
  with FunSuiteLike
  with ImplicitSender
  with BeforeAndAfterAll {

  implicit val actorMaterializer = ActorMaterializer()
  implicit val executor = system.dispatcher

  override def beforeAll {
    val eventBusPath = "akka.tcp://test-system@127.0.0.1:3653/user/event-handler"
    val mockEventHandler = system.actorOf(Props[MockEventHandler], "event-handler")
    val storeActor = system.actorOf(Props(classOf[StoreActor], (notification: Notification) => {}), "store")
    val notificationActor = system.actorOf(Props(classOf[NotificationActor]), "notification")
    val consumerActor = system.actorOf(Props(classOf[ConsumerActor], storeActor, notificationActor, eventBusPath), "consumer")
  }

  override def afterAll {
    shutdown()
  }

  test("consume and read message") {
    val consumerActor = system.actorSelection("akka.tcp://test-system@127.0.0.1:3653/user/consumer")
    val message = Message("system", "simpleReceiver", "simpleText", "simpleDate")
    consumerActor ! message

    Future {
      while (true) {
        val actor = system.actorSelection("akka.tcp://test-system@127.0.0.1:3653/user/notification/user-simpleReceiver")
        actor ! Read
      }
    }
    fishForMessage() {
      case Notifications(List(Notification("simpleReceiver", "simpleText", "simpleDate"))) => true
      case _ => false
    }
  }
}

class MockEventHandler extends Actor {
  def receive = {
    case s: Subscribe => {
      sender() ! Subscribed("system")
    }
  }
}