package todomvc.reactImpl

import japgolly.scalajs.react.{BackendScope, _}
import japgolly.scalajs.react.extra.router.{RouterCtl, _}
import japgolly.scalajs.react.vdom.all._
import org.scalajs.dom.ext.KeyValue
import todomvc._

object TodoList {

  case class Props(filter: Filter, ctl: RouterCtl[Filter])

  private val initialState = State(Seq())

  private class Backend($ : BackendScope[Props, State]) {

    def dispatch(a: TodoListAction): Callback = {
      if (a == NoOp)
        Callback(())
      else
        $.modState(s => update(a, s))
    }

    def tasksListElement(state: State, props: Props): ReactElement = {
      ul(cls := "todo-list")(
        state.todos
          .filter(getFilterFn(props.filter))
          .map(t => TodoListItem.component(TodoListItem.Props(t, dispatch)))
      )
    }

    def newTaskElement(state: State): ReactElement = {
      input(
        cls := "new-todo",
        placeholder := "What needs to be done?",
        onKeyUp ==> { e: ReactKeyboardEventI =>
          if (e.key == KeyValue.Enter && e.target.value.trim.nonEmpty) {
            val txt = e.target.value.trim
            e.target.value = ""
            dispatch(Add(txt))
          } else dispatch(NoOp)
        },
        autoFocus := state.todos.forall(_.editing.isEmpty)
      )
    }

    def toggleAllElement(state: State): ReactElement = {
      div(
        input(
          cls := "toggle-all",
          tpe := "checkbox",
          cursor := "pointer",
          checked := state.todos.nonEmpty && state.todos.forall(_.done),
          onClick --> {
            dispatch(ToggleAll)
          }
        ),
        label(`for` := "toggle-all", "Mark all as complete")
      )
    }

    def tasksListFooterElement(state: State, props: Props): ReactElement = {
      footer(cls := "footer")(
        span(cls := "todo-count")(
          strong(state.todos.count(!_.done)),
          " item left"
        ),
        ul(cls := "filters")(for (filter <- Filter.values) yield {
          li(a(cls := {
            if (filter == props.filter) "selected"
            else ""
          }, filter.toString, href := props.ctl.urlFor(filter).value))
        }),
        button(cls := "clear-completed", onClick --> {
          dispatch(ClearCompleted)
        }, "Clear completed (", state.todos.count(_.done), ")")
      )
    }

    def render(props: Props, state: State): ReactElement = div(
      section(cls := "todoapp")(
        header(cls := "header")(h1("todos"), newTaskElement(state)),
        section(cls := "main")(
          toggleAllElement(state),
          tasksListElement(state, props),
          tasksListFooterElement(state, props)
        )
      ),
      footer(cls := "info")(
        p("Double-click to edit a todo"),
        p(
          a(
            href := "https://github.com/lihaoyi/workbench-example-app/blob/todomvc/src/main/scala/example/ScalaJSExample.scala"
          )("Source Code")
        ),
        p("Created by ", a(href := "http://github.com/lihaoyi")("Li Haoyi"))
      )
    )
  }

  private val component = ReactComponentB[Props]("TodoMVC")
    .initialState(initialState)
    .renderBackend[Backend]
    .build

  private val routerConfig: RouterConfig[Filter] =
    RouterConfigDsl[Filter].buildConfig { dsl =>
      import dsl._

      def filterRule(route: String, filter: Filter): Rule =
        staticRoute(route, filter) ~> renderR(
          ctl => component(Props(filter, ctl))
        )

      (trimSlashes
        | filterRule("#all", Filter.All)
        | filterRule("#done", Filter.Done)
        | filterRule("#undone", Filter.Undone))
        .notFound(redirectToPage(Filter.All)(Redirect.Replace))

    }

  val router = Router(BaseUrl.until_#, routerConfig)
}
