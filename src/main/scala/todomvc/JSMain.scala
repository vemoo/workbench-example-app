package todomvc

import org.scalajs.dom.raw.Element

import scala.scalajs.js.annotation.JSExportTopLevel


object JSMain {

  def reactMain(element: Element): Unit = {
    reactImpl.TodoList.router().renderIntoDOM(element)
    ()
  }

  @JSExportTopLevel("todomvc.JSMain.main")
  def main(): Unit = {
    import org.scalajs.dom._
    document.body.innerHTML = ""
    val mainDiv = document.createElement("div")
    document.body.appendChild(mainDiv)

    reactMain(mainDiv)
  }
}
