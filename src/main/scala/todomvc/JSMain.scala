package todomvc

import org.scalajs.dom.raw.Element

import scala.scalajs.js.annotation.JSExport

@JSExport
object JSMain {

  def sriMain(element: Element): Unit = {
    sri.web.ReactDOM.render(sriImpl.TodoList(), element)
  }

  def reactMain(element: Element): Unit = {
    japgolly.scalajs.react.ReactDOM.render(reactImpl.TodoList.router(), element)
  }

  @JSExport
  def main(): Unit = {
    import org.scalajs.dom._
    document.body.innerHTML = ""
    val mainDiv = document.createElement("div")
    document.body.appendChild(mainDiv)

    reactMain(mainDiv)
  }
}
