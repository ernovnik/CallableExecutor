package ru.ernovnik

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Callable

import akka.actor.ActorSystem

object SchedulerTest extends App {

  implicit final val system = ActorSystem("scheduling")

  val scheduler = system.actorOf(SchedulerA.props)

  def test1() {
    scheduler ! (LocalDateTime.now, new Callable[String] {
      override def call(): String = "It works"
    })
  }

  def test1_1() {
    scheduler ! (LocalDateTime.now plus (1, ChronoUnit.SECONDS), new Callable[String] {
      override def call(): String = "It works"
    })
  }

  def test2() {
    for (i <- 1 to 10)
      scheduler ! (LocalDateTime.now, new Callable[String] {
        override def call(): String = s"$i: It works"
      })
  }

  def test3() {
    for (i <- 1 to 10)
      scheduler ! (LocalDateTime.now plus(i, ChronoUnit.SECONDS), new Callable[String] {
        override def call(): String = {
          s"$i: It works"
        }
      })
  }

  def test4() {
    for (i <- 1 to 10)
      scheduler ! (LocalDateTime.now plus(10 - i, ChronoUnit.SECONDS), new Callable[String] {
        override def call(): String = {
          s"$i: It works"
        }
      })
  }

  test4()

}
