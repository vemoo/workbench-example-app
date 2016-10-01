package example

import japgolly.scalajs.react.ReactDOM

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

object ScalaJSExample extends JSApp {

  @JSExport
  def main(): Unit = {
    import org.scalajs.dom.document
    document.body.innerHTML = ""
    val main = document.createElement("div")
    document.body.appendChild(main)

    ReactDOM.render(TodoList.router(), main)
  }
}
