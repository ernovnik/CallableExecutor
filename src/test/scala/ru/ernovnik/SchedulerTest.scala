package com.scalafirst.akka.sheduling

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer

object SchedulerTest extends App {

  implicit final val system = ActorSystem("scheduling")
  implicit val materializer = ActorMaterializer()

  val scheduler = system.actorOf(Props[SchedulerA])

  def test1() {
    scheduler ! Task(LocalDateTime.now, () => "It works")
  }

  def test1_1() {
    scheduler ! Task(LocalDateTime.now plus(1, ChronoUnit.SECONDS), () => "It works")
  }

  def test2() {
    for (i <- 1 to 10)
      scheduler ! Task(LocalDateTime.now, () => s"$i: It works")
  }

  def test3() {
    for (i <- 1 to 10)
      scheduler ! Task(LocalDateTime.now plus(i, ChronoUnit.SECONDS), () => s"$i: It works")
  }

  def test4() {
    for (i <- 1 to 10)
      scheduler ! Task(LocalDateTime.now plus(10 - i, ChronoUnit.SECONDS), () => s"$i: It works")
  }

  test4()

}
