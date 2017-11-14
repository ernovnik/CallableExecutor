package ru.ernovnik

import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.Callable

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, _}

case class Task(time: Long, callable: Callable[String]) {
  def delay: FiniteDuration = {
    val d = math.max(time - LocalDateTime.now.toInstant(ZoneOffset.UTC).toEpochMilli, 0)
    d millis
  }

//  def ready: Boolean = time < LocalDateTime.now.toInstant(ZoneOffset.UTC).toEpochMilli
}

case object Execute


object SchedulerA {
  def props: Props = Props(new SchedulerA())
}

class SchedulerA extends Actor with ActorLogging {

  val executor: ActorRef = context.system.actorOf(ExecutorA.props)

  val q: ListBuffer[Task] = ListBuffer.empty

  var scheduled: Option[Cancellable] = None

  def addToQueue(task: Task) {
    val afterIndex = q.indexWhere(_.time > task.time)
    if (afterIndex >= 0) {
      // Добавляется перед другими задачами
      q.insert(afterIndex, task)
    } else {
      // Добавляется в конец
      q.append(task)
    }
    reschedule()
  }

  def reschedule() {
    scheduled.foreach(_.cancel())
    scheduled = for (task <- q.headOption) yield
      context.system.scheduler.scheduleOnce(task.delay, self, Execute)
  }

  override def receive: Receive = {
    case (time: LocalDateTime, callable: Callable[String]) =>
      val task = Task(time.toInstant(ZoneOffset.UTC).toEpochMilli, callable)
      addToQueue(task)
    case Execute =>
      for (head <- q.headOption) {
        q.trimStart(1)
        reschedule()
        executor ! head.callable
      }
  }
}
