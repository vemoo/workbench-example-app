package example

import japgolly.scalajs.react.ReactDOM

import scala.scalajs.js.JSApp

object ScalaJSExample extends JSApp {

  def main(): Unit = {
    import org.scalajs.dom._
    document.body.innerHTML = ""
    val main = document.createElement("div")
    document.body.appendChild(main)

    ReactDOM.render(TodoList.router(), main)
  }
}
