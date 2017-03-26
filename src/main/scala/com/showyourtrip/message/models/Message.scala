package com.showyourtrip.message.models

import akka.actor.ActorRef

case class Message(senderId: String, receiverId: String, text: String, date: String)

case class Subscribe(topic: String, subscriber: ActorRef)

case class Subscribed(topic: String)
