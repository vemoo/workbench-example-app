package todomvc.reactImpl

import japgolly.scalajs.react.{BackendScope, _}
import japgolly.scalajs.react.extra.router.{RouterCtl, _}
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom.ext.KeyValue
import todomvc._

object TodoList {

  case class State(tasks: Seq[Task], editing: Option[Task])
  case class Props(filter: Filter, ctl: RouterCtl[Filter])


  val initialState = State(Seq(), None)


  class Backend($: BackendScope[Props, State]) {
    def render(props: Props, state: State) = div(
      section(cls := "todoapp")(
        header(cls := "header")(
          h1("todos"),
          input(
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
            for (task <- state.tasks if getFilterFn(props.filter)(task)) yield {
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
              for (filter <- Filter.values) yield {
                li(a(
                  cls := {
                    if (filter == props.filter) "selected"
                    else ""
                  },
                  filter.toString,
                  href := props.ctl.urlFor(filter).value
                ))
              }
            ),
            button(
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


  val component = ReactComponentB[Props]("TodoMVC")
    .initialState(initialState)
    .renderBackend[Backend]
    .build

  val routerConfig = RouterConfigDsl[Filter].buildConfig { dsl =>
    import dsl._

    def filterRule(route: String, filter: Filter): Rule =
      staticRoute(route, filter) ~> renderR(ctl => component(Props(filter, ctl)))

    (trimSlashes
      | filterRule("#all", Filter.All)
      | filterRule("#done", Filter.Done)
      | filterRule("#undone", Filter.Undone)
      )
      .notFound(redirectToPage(Filter.All)(Redirect.Replace))

  }

  val router = Router(BaseUrl.until_#, routerConfig)
}
