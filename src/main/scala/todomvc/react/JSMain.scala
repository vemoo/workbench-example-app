package todomvc.react

import japgolly.scalajs.react.ReactDOM

import scala.scalajs.js.annotation.JSExport

@JSExport
object JSMain {

  @JSExport
  def main(): Unit = {
    import org.scalajs.dom._
    document.body.innerHTML = ""
    val main = document.createElement("div")
    document.body.appendChild(main)

    ReactDOM.render(TodoList.router(), main)
  }
}
