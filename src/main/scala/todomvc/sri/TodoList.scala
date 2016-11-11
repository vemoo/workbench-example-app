package todomvc.sri

import enumeratum.{Enum, EnumEntry}
import org.scalajs.dom.ext.KeyValue
import sri.core.{ReactComponent, ReactElement}
import sri.web.vdom.htmltagsNoInline._
import sri.web.all._

import scalajs.js.annotation.ScalaJSDefined

object TodoList {

  case class Task(id: Int, txt: String, done: Boolean) {
    override def equals(obj: scala.Any): Boolean = obj match {
      case Task(otherId, _, _) => id == otherId
      case _ => false
    }
  }

  sealed trait Filter extends EnumEntry

  object Filter extends Enum[Filter] {
    val values = findValues

    case object All extends Filter

    case object Done extends Filter

    case object Undone extends Filter

  }

  def getFilterFn(f: Filter): Task => Boolean = f match {
    case Filter.All => _ => true
    case Filter.Done => _.done
    case Filter.Undone => !_.done
  }

  case class State(tasks: Seq[Task], editing: Option[Task])

  case class Props(filter: Filter)


  @ScalaJSDefined
  class Component extends ReactComponent[Props, State] {
    initialState(State(Seq(), None))

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
                  val newId = if (state.tasks.isEmpty) 1 else state.tasks.map(_.id).max + 1
                  val newTask = Task(newId, e.target.value, false)
                  e.target.value = ""
                  setState(state.copy(tasks = newTask +: state.tasks))
                }
              },
              autoFocus = state.editing.isEmpty
            )
          ), section(className = "main")(
            input(className = "toggle-all",
              `type` = "checkbox",
              checked = if (state.tasks.nonEmpty && state.tasks.forall(_.done)) "checked" else "",
              onClick = { e: ReactMouseEventI =>
                setState(state.copy(tasks = state.tasks.map(_.copy(done = e.target.checked))))
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
                    onDoubleClick = { e: ReactEvent =>
                      setState(state.copy(editing = Some(task)))
                    })(
                    input(className = "toggle", `type` = "checkbox",
                      onChange = { e: ReactEvent =>
                        setState(state.copy(tasks = state.tasks.map { t =>
                          if (t == task)
                            t.copy(done = !t.done)
                          else t
                        }))
                      },
                      checked = if (task.done) "checked" else ""),
                    label()(task.txt),
                    button(
                      className = "destroy",
                      onClick = { e: ReactEvent =>
                        setState(state.copy(tasks = state.tasks.filter(_ != task)))
                      }
                    )()),
                  state.editing match {
                    case Some(editing) => input(
                      className = "edit", value = editing.txt,
                      onInput = { e: ReactEventI =>
                        setState(state.copy(editing = state.editing.map(_.copy(txt = e.target.value.trim))))
                      },
                      onKeyUp = { e: ReactKeyboardEventI =>
                        if (e.key == KeyValue.Enter)
                          setState(state.copy(tasks = state.tasks.map { t =>
                            if (t == editing)
                              t.copy(txt = editing.txt)
                            else t
                          }, editing = None))
                        else if (e.key == KeyValue.Escape)
                          setState(state.copy(editing = None))
                      },
                      onBlur = { e: ReactEvent =>
                        setState(state.copy(editing = None))
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
                onClick = { e: ReactEvent => setState(state.copy(tasks = state.tasks.filter(!_.done))) })(
                "Clear completed (", state.tasks.count(_.done), ")"
              )
            )
          )
        )
      )
    }
  }

  def apply() = makeElement[Component](Props(Filter.All))
}

