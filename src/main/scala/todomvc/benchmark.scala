package todomvc

import org.scalajs.dom.raw._
import org.scalajs.dom.{document, window}
import monix.execution.Scheduler.Implicits.global
import monix.eval._
import monix.execution.Cancelable

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

@JSExport
object benchmark {

  val inputEv: Event = {
    val e = document.createEvent("Events")
    e.initEvent("input", canBubbleArg = true, cancelableArg = true)
    e
  }

  val enterDownEvent: Event = {
    val e = document.createEvent("Events")
    e.initEvent("keydown", canBubbleArg = true, cancelableArg = true)
    val eDyn = e.asInstanceOf[js.Dynamic]
    eDyn.keyCode = 13
    eDyn.which = 13
    e
  }

  val enterUpEvent: Event = {
    val e = document.createEvent("Events")
    e.initEvent("keyup", canBubbleArg = true, cancelableArg = true)
    val eDyn = e.asInstanceOf[js.Dynamic]
    eDyn.keyCode = 13
    eDyn.which = 13
    e
  }

  def doEnter(el: Element): Unit = {
    el.dispatchEvent(inputEv)
    el.dispatchEvent(enterDownEvent)
    el.dispatchEvent(enterUpEvent)
  }

  def interleavedForEach[T](xs: Iterable[T], action: T => Unit): Task[Unit] = {
    Task.create[Unit] { (_, callback) =>

      val it = xs.iterator

      var handle = 0

      def step(): Unit = {
        if (!it.hasNext)
          callback.onSuccess(())
        else
          handle = window.setTimeout(() => {
            action(it.next())
            step()
          }, 0)
      }

      step()

      Cancelable { () => window.clearTimeout(handle) }
    }
  }

  def delay(millis: Double): Task[Unit] = {
    Task.create[Unit] { (_, callback) =>
      val handle = window.setTimeout(() => callback.onSuccess(()), millis)
      Cancelable { () => window.clearTimeout(handle) }
    }
  }

  def delay0(): Task[Unit] = delay(0)

  def add100(): Task[Unit] = {
    val newTodo =
      document.querySelector(".new-todo").asInstanceOf[HTMLInputElement]
    interleavedForEach(1 to 100, (i: Int) => {
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

  def whileExistsSelector[T <: Element](selector: String): Iterable[T] = new Iterable[T] {
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
        _ <- add100()
        _ <- toggleAll()
        _ <- destroyAll()
      } yield {
        window.performance.now() - t0
      }
    }
  }

  @JSExport
  def run(times: Int = 3): Unit = {
    val benches = (0 until times).map(_ => benchmark())
    Task.sequence(benches).foreach(ts => println(s"${ts.sum / ts.size} ms"))
  }

}
