package example

import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{BackendScope, ReactComponentB, ReactDOM, _}
import org.scalajs.dom.ext.KeyValue

import scala.scalajs.js.annotation.JSExport
import scalajs.js
import org.scalajs.dom.console

case class Task(id: Int, txt: String, done: Boolean) {
  override def equals(obj: scala.Any): Boolean = obj match {
    case Task(otherId, _, _) => id == otherId
    case _ => false
  }
}

case class State(tasks: Seq[Task], editing: Option[Task], filter: String)

@JSExport
object ScalaJSExample {

  val initialState = State(
    Seq(),
    None,
    "All"
  )

  val filters: Map[String, Task => Boolean] = Map(
    "All" -> (_ => true),
    "Done" -> (_.done),
    "Undone" -> (!_.done)
  )

  object react {

    class Backend($: BackendScope[Unit, State]) {
      def render(state: State) = div(
        section(cls := "todoapp")(
          header(cls := "header")(
            h1("todos"),
            input(
              id := "new-todo",
              cls := "new-todo",
              placeholder := "What needs to be done?",
              onKeyUp ==> { e: ReactKeyboardEventI =>
                if (e.key == KeyValue.Enter && e.target.value.trim.nonEmpty) {
                  val newId = if (state.tasks.isEmpty) 1 else state.tasks.map(_.id).max + 1
                  val newTask = Task(newId, e.target.value.trim, false)
                  e.target.value = ""
                  $.setState(state.copy(tasks = newTask +: state.tasks))
                }
                else Callback(())
              },
              autoFocus := state.editing.isEmpty
            )
          ),
          section(cls := "main")(
            input(
              id := "toggle-all",
              cls := "toggle-all",
              tpe := "checkbox",
              cursor := "pointer",
              checked := state.tasks.nonEmpty && state.tasks.forall(_.done),
              onClick ==> { e: ReactUIEventI =>
                $.setState(state.copy(tasks = state.tasks.map(_.copy(done = e.target.checked))))
              }
            ),
            label(`for` := "toggle-all", "Mark all as complete"),
            ul(cls := "todo-list")(
              for (task <- state.tasks if filters(state.filter)(task)) yield {
                li(
                  cls := {
                    if (task.done) "completed"
                    else if (state.editing.contains(task)) "editing"
                    else ""
                  },
                  div(cls := "view")(
                    onDblClick --> {
                      $.setState(state.copy(editing = Some(task)))
                    },
                    input(
                      cls := "toggle",
                      tpe := "checkbox",
                      cursor := "pointer",
                      onChange --> {
                        $.setState(state.copy(tasks = state.tasks.map { t =>
                          if (t == task)
                            t.copy(done = !t.done)
                          else t
                        }))
                      },
                      checked := task.done
                    ),
                    label(task.txt),
                    button(
                      cls := "destroy",
                      cursor := "pointer",
                      onClick --> {
                        $.setState(state.copy(tasks = state.tasks.filter(_ != task)))
                      }
                    )
                  ),
                  state.editing match {
                    case Some(editing) =>
                      input(cls := "edit", value := editing.txt,
                        onInput ==> { e: ReactEventI =>
                          $.setState(state.copy(editing = state.editing.map(_.copy(txt = e.target.value.trim))))
                        },
                        onKeyUp ==> { e: ReactKeyboardEventI =>
                          if (e.key == KeyValue.Enter)
                            $.setState(state.copy(tasks = state.tasks.map { t =>
                              if (t == editing)
                                t.copy(txt = editing.txt)
                              else t
                            }, editing = None))
                          else if (e.key == KeyValue.Escape)
                            $.setState(state.copy(editing = None))
                          else
                            Callback(())
                        },
                        onBlur --> {
                          $.setState(state.copy(editing = None))
                        },
                        autoFocus := true
                      )
                    case None => ""
                  }
                )
              }
            ),
            footer(cls := "footer")(
              span(cls := "todo-count")(strong(state.tasks.count(!_.done)), " item left"),
              ul(cls := "filters")(
                for ((name, pred) <- filters.toSeq) yield {
                  li(a(
                    cls := {
                      if (name == state.filter) "selected"
                      else ""
                    },
                    name,
                    href := "#",
                    onClick --> {
                      $.setState(state.copy(filter = name))
                    }
                  ))
                }
              ),
              button(
                id := "clear-completed",
                cls := "clear-completed",
                onClick --> {
                  $.setState(state.copy(tasks = state.tasks.filter(!_.done)))
                },
                "Clear completed (", state.tasks.count(_.done), ")"
              )
            )
          )
        ),
        footer(cls := "info")(
          p("Double-click to edit a todo"),
          p(a(href := "https://github.com/lihaoyi/workbench-example-app/blob/todomvc/src/main/scala/example/ScalaJSExample.scala")("Source Code")),
          p("Created by ", a(href := "http://github.com/lihaoyi")("Li Haoyi"))
        )
      )
    }


    val component = ReactComponentB[Unit]("TodoMVC")
      .initialState(initialState)
      .renderBackend[Backend]
      .build
  }


  def bench: Unit = {
    import org.scalajs.dom.window._
    import org.scalajs.dom.document._
    import org.scalajs.dom.raw._

    val enterEvent: KeyboardEvent = {
      scalajs.js.Dynamic.newInstance(scalajs.js.Dynamic.global.KeyboardEvent)("keyup",
        scalajs.js.Dynamic.literal(
          "key" -> "Enter",
          "code" -> "Enter",
          "bubbles" -> true
        )
      ).asInstanceOf[KeyboardEvent]
    }

    val clickEvent: MouseEvent = {
      scalajs.js.Dynamic.newInstance(scalajs.js.Dynamic.global.MouseEvent)("click",
        scalajs.js.Dynamic.literal("bubbles" -> true)
      ).asInstanceOf[MouseEvent]
    }

    def add100(done: => Unit): Unit = {
      def it(inp: HTMLInputElement, i: Int, lim: Int, done: => Unit): Unit = {
        def enter: Unit = {
          setTimeout(() => {
            inp.dispatchEvent(enterEvent)
            it(inp, i + 1, lim, done)
          }, 0)
        }
        setTimeout(() => {
          if (i < lim) {
            inp.value = s"Todo number $i"
            enter
          } else done
        }, 0)
      }

      val inp = getElementById("new-todo").asInstanceOf[HTMLInputElement]
      it(inp, 0, 100, done)
    }

    def toggleAll(done: => Unit): Unit = {

      def it(ns: NodeList, i: Int, done: => Unit): Unit = {
        setTimeout({ () =>
          if (i < ns.length) {
            ns(i).dispatchEvent(clickEvent)
            it(ns, i + 1, done)
          }
          else done
        }, 0)
      }
      val all = querySelectorAll("input.toggle")
      it(all, 0, done)
    }

    def clear(done: => Unit): Unit = {
      getElementById("clear-completed").dispatchEvent(clickEvent)
      setTimeout(done _, 0)
    }

    val t0 = performance.now()
    add100(toggleAll(clear({
      val t1 = performance.now()
      alert(s"${t1 - t0}")
    })))
  }

  @JSExport
  def main(): Unit = {
    import org.scalajs.dom.document
    import org.scalajs.dom.raw._
    document.body.innerHTML = ""

    val btnBench = document.createElement("button").asInstanceOf[HTMLButtonElement]
    btnBench.textContent = "bench"
    btnBench.onclick = { _: MouseEvent => bench }
    document.body.appendChild(btnBench)

    val main = document.createElement("div")
    document.body.appendChild(main)

    ReactDOM.render(react.component(), main)
  }
}
