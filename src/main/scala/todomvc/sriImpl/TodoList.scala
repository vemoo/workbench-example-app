/*
package todomvc.sriImpl

import org.scalajs.dom.ext.KeyValue
import sri.core.{ReactComponent, ReactElement}
import sri.web.all._
import sri.web.styles.WebStyleSheet
import sri.web.vdom.htmltagsNoInline._
import todomvc._

import scala.scalajs.js.annotation.ScalaJSDefined

object TodoList {

  case class Props(filter: Filter)

  object MyStyles extends WebStyleSheet {
    val pointer = style(
      cursor := "pointer"
    )
  }

  @ScalaJSDefined
  class Component extends ReactComponent[Props, State] {
    initialState(State(Seq(), None))

    def dispatch(a: TodoAction): Unit = {
      val newState = update(a, state)
      setState(newState)
    }

    def render(): ReactElement = {
      div()(
        section(className = "todoapp")(
          header(className = "header")(
            h1()("todos"),
            input(
              className = "new-todo",
              placeholder = "Whats needs to be done?",
              onKeyUp = { e: ReactKeyboardEventI =>
                if (e.key == KeyValue.Enter && e.target.value.trim.nonEmpty) {
                  val txt = e.target.value.trim
                  e.target.value = ""
                  dispatch(Add(txt))
                }
              },
              autoFocus = state.editing.isEmpty
            )
          ), section(className = "main")(
            input(className = "toggle-all",
              `type` = "checkbox",
              style = MyStyles.pointer,
              checked = if (state.tasks.nonEmpty && state.tasks.forall(_.done)) "checked" else "",
              onClick = { _: ReactMouseEventI =>
                dispatch(ToggleAll)
              }
            ),
            label(htmlFor = "toggle-all")("Mark all as complete"),
            ul(className = "todo-list")(
              for (task <- state.tasks if getFilterFn(props.filter)(task)) yield {
                li(className = {
                  if (task.done) "completed"
                  else if (state.editing.contains(task)) "editing"
                  else ""
                })(
                  div(className = "view",
                    onDoubleClick = { _: ReactEvent =>
                      dispatch(SetEditing(task))
                    })(
                    input(className = "toggle", `type` = "checkbox",
                      style = MyStyles.pointer,
                      onChange = { _: ReactEvent =>
                        dispatch(Toggle(task))
                      },
                      checked = if (task.done) "checked" else ""),
                    label()(task.txt),
                    button(
                      className = "destroy",
                      style = MyStyles.pointer,
                      onClick = { _: ReactEvent =>
                        dispatch(Delete(task))
                      }
                    )()),
                  state.editing match {
                    case Some(editing) => input(
                      className = "edit", value = editing.txt,
                      onInput = { e: ReactEventI =>
                        dispatch(UpdateEditingText(e.target.value.trim))
                      },
                      onKeyUp = { e: ReactKeyboardEventI =>
                        if (e.key == KeyValue.Enter)
                          dispatch(ConfirmEditing)
                        else if (e.key == KeyValue.Escape)
                          dispatch(CancelEditing)
                      },
                      onBlur = { _: ReactEvent =>
                        dispatch(CancelEditing)
                      },
                      autoFocus = true
                    )
                    case None => ""
                  }
                )
              }
            ),
            footer(className = "footer")(
              span(className = "todo-count")(strong()(state.tasks.count(!_.done)), " item left"),
              ul(className = "filters")(
                for (filter <- Filter.values) yield {
                  li()(a(
                    className = if (filter == props.filter) "selected" else "",
                    href = "#TODO")(
                    filter.toString
                  ))
                }
              ), button(className = "clear-completed",
                onClick = { _: ReactEvent =>
                  dispatch(ClearCompleted)
                })(
                "Clear completed (", state.tasks.count(_.done), ")"
              )
            )
          )
        )
      )
    }
  }

  def apply() = makeElement[Component](Props(Filter.All))
}*/
