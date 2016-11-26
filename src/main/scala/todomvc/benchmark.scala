package todomvc

import org.scalajs.dom.raw._
import org.scalajs.dom.{document, window}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.Success

@JSExport
object benchmark {

  val inputEv: Event = {
    val e = document.createEvent("Events")
    e.initEvent("input", true, true)
    e
  }

  val enterDownEvent: Event = {
    val e = document.createEvent("Events")
    e.initEvent("keydown", true, true)
    e.asInstanceOf[js.Dynamic].keyCode = 13
    e
  }

  val enterUpEvent: Event = {
    val e = document.createEvent("Events")
    e.initEvent("keyup", true, true)
    e.asInstanceOf[js.Dynamic].keyCode = 13
    e
  }

  def doEnter(el: Element): Unit = {
    el.dispatchEvent(inputEv)
    el.dispatchEvent(enterDownEvent)
    el.dispatchEvent(enterUpEvent)
  }

  def doForEach[T](xs: Iterable[T], action: T => Unit): Future[Unit] = {
    val promise = Promise[Unit]

    val it = xs.iterator

    def step(): Unit = {
      if (!it.hasNext)
        promise.complete(Success(()))
      else
        window.setTimeout(() => {
          action(it.next())
          step()
        }, 0)
    }

    step()

    promise.future
  }

  def delay(millis: Double): Future[Unit] = {
    val promise = Promise[Unit]
    window.setTimeout(() => promise.complete(Success(())), millis)
    promise.future
  }

  def delay0(): Future[Unit] = delay(0)

  def add100(): Future[Unit] = {
    val newTodo =
      document.querySelector(".new-todo").asInstanceOf[HTMLInputElement]
    doForEach(1 to 100, (i: Int) => {
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

  def toggleAll(): Future[Unit] = {
    val toggles = nodeListToIterable(document.querySelectorAll(".toggle"))
    doForEach(toggles, (node: Node) => {
      node.asInstanceOf[HTMLButtonElement].click()
    })
  }

  def destroyAll(): Future[Unit] = {
    val destroyButtons = new Iterable[HTMLButtonElement] {
      def iterator = new Iterator[HTMLButtonElement] {

        def button(): Element = document.querySelector(".destroy")

        def hasNext: Boolean = button() != null

        def next(): HTMLButtonElement =
          button().asInstanceOf[HTMLButtonElement]
      }
    }

    doForEach(destroyButtons, (b: HTMLButtonElement) => b.click())
  }

  def benchmark(): Future[Double] = {
    val t0 = window.performance.now()
    for {
      _ <- add100()
      _ <- toggleAll()
      _ <- destroyAll()
    } yield {
      window.performance.now() - t0
    }
  }

  def linearize[T](fs: Seq[() => Future[T]]): Future[Seq[T]] = {
    fs.foldLeft(Future.successful(Seq.newBuilder[T])) { (fb, fx) =>
      for {
        b <- fb
        x <- fx()
      } yield b += x
    }
      .map(_.result())
  }

  @JSExport
  def run(times: Int = 3): Unit = {
    val benches = (0 until times).map(_ => () => benchmark())
    linearize(benches).foreach(ts => println(s"${ts.sum / ts.size} ms"))
  }
}
