package todomvc

import java.util.concurrent.TimeUnit

import org.scalajs.dom.raw._
import org.scalajs.dom.{document, window}
import monix.execution.Scheduler.Implicits.global
import monix.eval._
import monix.execution.Cancelable

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel



object benchmark {

  def createEvent(eType: String, attrs: (String, js.Any)*): Event = {
    val e = document.createEvent("Events")
    e.initEvent(eType, canBubbleArg = true, cancelableArg = true)
    attrs.foreach { case (k, v) =>
      e.asInstanceOf[js.Dynamic].updateDynamic(k)(v)
    }
    e
  }

  val inputEv: Event = createEvent("input")
  val enterDownEvent: Event = createEvent("keydown", "keyCode" -> 13)
  val enterPressEvent: Event = createEvent("keypress", "keyCode" -> 13)
  val enterUpEvent: Event = createEvent("keyup", "keyCode" -> 13)

  def doEnter(el: Element): Unit = {
    val events = Seq(inputEv, enterDownEvent, enterPressEvent, enterUpEvent)
    events.foreach(el.dispatchEvent)
  }

  def interleavedForEach[T](xs: Iterable[T], action: T => Unit): Task[Unit] = {
    Task.create[Unit] { (scheduler, callback) =>
      val it = xs.iterator

      var cancelable = Cancelable.empty

      def step(): Unit = {
        if (!it.hasNext)
          callback.onSuccess(())
        else
          cancelable = scheduler.scheduleOnce(Duration.Zero) {
            action(it.next())
            step()
          }
      }

      step()

      cancelable
    }
  }

  def delay(millis: Long): Task[Unit] = {
    Task.create[Unit] { (scheduler, callback) =>
      scheduler.scheduleOnce(FiniteDuration(millis, TimeUnit.MILLISECONDS)) {
        callback.onSuccess(())
      }
    }
  }

  def delay0(): Task[Unit] = delay(0)

  def add(n: Int): Task[Unit] = {
    val newTodo = {
      val x = document.querySelector(".new-todo")
      if (x == null) document.querySelector("#new-todo")
      else x
    }.asInstanceOf[HTMLInputElement]
    interleavedForEach(1 to n, (i: Int) => {
      newTodo.value = s"Task number $i"
      doEnter(newTodo)
    })
  }

  def nodeListToIterable(nodeList: NodeList): Iterable[Node] =
    new Iterable[Node] {
      def iterator: Iterator[Node] = new Iterator[Node] {
        var i = 0

        def hasNext: Boolean = nodeList.length > i

        def next(): Node = {
          val x = nodeList(i)
          i += 1
          x
        }
      }
    }

  def whileExistsSelector[T <: Element](selector: String): Iterable[T] =
    new Iterable[T] {
      def iterator: Iterator[T] = new Iterator[T] {

        private def elem() = document.querySelector(selector)

        def hasNext: Boolean = elem() != null

        def next(): T = elem().asInstanceOf[T]
      }
    }

  def toggleAll(): Task[Unit] = {
    val toggles = nodeListToIterable(document.querySelectorAll(".toggle"))
    interleavedForEach(toggles, (node: Node) => {
      node.asInstanceOf[HTMLButtonElement].click()
    })
  }

  def destroyAll(): Task[Unit] = {
    val destroyButtons = whileExistsSelector[HTMLButtonElement](".destroy")
    interleavedForEach(destroyButtons, (b: HTMLButtonElement) => b.click())
  }

  def benchmark(): Task[Double] = {
    Task.defer {
      val t0 = window.performance.now()
      for {
        _ <- add(100)
        _ <- toggleAll()
        _ <- destroyAll()
      } yield {
        window.performance.now() - t0
      }
    }
  }

  @JSExportTopLevel("todomvc.benchmark.run")
  def run(times: Int = 3): Unit = {
    val benches = (0 until times).map(_ => benchmark())
    Task.sequence(benches).foreach(ts => println(s"${ts.sum / ts.size} ms"))
  }

  //run(1)
}
