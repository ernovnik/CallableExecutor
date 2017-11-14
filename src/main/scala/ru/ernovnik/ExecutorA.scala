package ru.ernovnik

import java.util.concurrent.Callable

import akka.actor.{Actor, ActorLogging, Props}

object ExecutorA {
  def props: Props = Props(new ExecutorA())
}

class ExecutorA extends Actor with ActorLogging {

  override def receive: Receive = {
    case task: Callable[String] =>
      val res = task.call()
      log.info(res)
  }
}
