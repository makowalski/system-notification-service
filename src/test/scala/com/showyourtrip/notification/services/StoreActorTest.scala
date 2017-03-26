package com.showyourtrip.notification.services

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import com.showyourtrip.notification.models.Notification
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}


class StoreActorTest extends TestKit(ActorSystem("test-system"))
  with FunSuiteLike
  with MockFactory
  with BeforeAndAfterAll {

  override def afterAll {
    shutdown()
  }

  test("should invoke insertFunction when notification arrives") {

    val insertFunctionMock = mockFunction[Notification, Unit]
    val storeActor = system.actorOf(Props(classOf[StoreActor], insertFunctionMock))
    val notification = Notification("userId", "test message", "test date")
    insertFunctionMock.expects(notification)

    storeActor ! notification
  }
}
