package todomvc.reactImpl

import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.all._
import japgolly.scalajs.react.{
  Callback,
  ScalaComponent,
  ReactEventFromInput,
  ReactKeyboardEventFromInput
}
import org.scalajs.dom.ext.KeyValue
import todomvc.TodoMVC._

object TodoListItem {
  case class Props(task: Todo, dispatch: TodoItemAction => Callback)

  private def render(p: Props): VdomElement = {
    li(
      cls := {
        if (p.task.done) "completed"
        else if (p.task.editing.nonEmpty) "editing"
        else ""
      },
      div(cls := "view")(
        onDblClick --> {
          p.dispatch(StartEditing(p.task))
        },
        input(
          cls := "toggle",
          tpe := "checkbox",
          cursor := "pointer",
          onChange --> {
            p.dispatch(Toggle(p.task))
          },
          checked := p.task.done
        ),
        label(p.task.txt),
        button(cls := "destroy", cursor := "pointer", onClick --> {
          p.dispatch(Delete(p.task))
        })
      ),
      p.task.editing match {
        case Some(editing) =>
          input(cls := "edit", value := editing, onInput ==> {
            e: ReactEventFromInput =>
              p.dispatch(UpdateEditing(p.task, e.target.value.trim))
          }, onKeyUp ==> { e: ReactKeyboardEventFromInput =>
            if (e.key == KeyValue.Enter)
              p.dispatch(ConfirmEditing(p.task))
            else if (e.key == KeyValue.Escape)
              p.dispatch(CancelEditing(p.task))
            else
              p.dispatch(NoOp)              
          }, onBlur --> {
            p.dispatch(CancelEditing(p.task))
          }, autoFocus := true)
        case None => ""
      }
    )
  }

  private implicit val taskReuse: Reusability[Todo] = Reusability.by_==
  private implicit val propsReuse: Reusability[Props] = Reusability.by(_.task)
  //noinspection TypeAnnotation
  val component = ScalaComponent.builder[Props]("TodoListItem")
    .render_P(render)
    .configure(Reusability.shouldComponentUpdate)
    .build
}
