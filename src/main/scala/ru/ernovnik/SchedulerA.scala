package com.scalafirst.akka.sheduling

import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.Callable

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class Task(localTime: LocalDateTime, callable: Callable[String]) {
  val time: Long = localTime.toInstant(ZoneOffset.UTC).toEpochMilli

  def delay: FiniteDuration = math.max(time - LocalDateTime.now.toInstant(ZoneOffset.UTC).toEpochMilli, 0) millis
}

case object Execute

class SchedulerA extends Actor with ActorLogging {

  val executor: ActorRef = context.system.actorOf(Props[ExecutorA])

  val queue: ListBuffer[Task] = ListBuffer.empty

  var scheduled: Option[Cancellable] = None

  def addToQueue(task: Task) {
    val biggerIndex = queue.indexWhere(_.time > task.time)
    val insertIndex = if (biggerIndex == -1) queue.length // В конец очереди, если не найдено бОльшее значение
    else biggerIndex // Добавляется перед другими задачами

    queue insert(insertIndex, task)
    if (insertIndex == 0) reschedule() // Если добавлено в голову - переопределяем следующий запуск
  }

  def reschedule() {
    scheduled.foreach(_.cancel())
    scheduled = for (task <- queue.headOption) yield
      context.system.scheduler.scheduleOnce(task.delay, self, Execute)
  }

  def execute() {
    for (head <- queue.headOption) {
      queue trimStart 1
      reschedule()

      executor ! head.callable
    }
  }

  override def receive: Receive = {
    case task: Task => addToQueue(task)
    case Execute => execute()
  }

}
