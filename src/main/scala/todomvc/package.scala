import enumeratum.{Enum, EnumEntry}

package object todomvc {

  case class Todo(txt: String, done: Boolean, editing: Option[String]) {
    //todos with same contents can be different todos
    override def equals(obj: scala.Any): Boolean = obj match {
      case ar: AnyRef => ar.eq(this)
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

  def getFilterFn(f: Filter): Todo => Boolean = f match {
    case Filter.All =>
      _ =>
        true
    case Filter.Done => _.done
    case Filter.Undone => !_.done
  }

  case class State(todos: Seq[Todo])

  sealed trait TodoListAction

  sealed trait TodoItemAction extends TodoListAction

  case class Toggle(todo: Todo) extends TodoItemAction
  case class Delete(todo: Todo) extends TodoItemAction
  case class StartEditing(todo: Todo) extends TodoItemAction
  case class UpdateEditing(todo: Todo, newTxt: String) extends TodoItemAction
  case class ConfirmEditing(todo: Todo) extends TodoItemAction
  case class CancelEditing(todo: Todo) extends TodoItemAction

  case class Add(txt: String) extends TodoListAction
  case object ToggleAll extends TodoListAction
  case object ClearCompleted extends TodoListAction
  case object NoOp extends TodoListAction

  private def updateTodo(task: Todo, update: Todo => Todo, s: State): State = {
    val newTasks = s.todos.map(t => if (t == task) update(t) else t)
    s.copy(todos = newTasks)
  }

  def update(a: TodoListAction, s: State): State = a match {
    case Add(txt) =>
      val newTodo = Todo(txt, done = false, editing = None)
      s.copy(todos = newTodo +: s.todos)

    case Toggle(todo) =>
      updateTodo(todo, t => t.copy(done = !t.done), s)

    case Delete(todo) =>
      val newTodo = s.todos.filterNot(_ == todo)
      s.copy(todos = newTodo)

    case StartEditing(todo) =>
      updateTodo(todo, t => t.copy(editing = Some(t.txt)), s)

    case UpdateEditing(todo, newTxt) =>
      updateTodo(todo, t => t.copy(editing = Some(newTxt)), s)

    case ConfirmEditing(todo) =>
      updateTodo(todo,
                 t => t.copy(txt = t.editing.getOrElse(t.txt), editing = None),
                 s)

    case CancelEditing(todo) =>
      updateTodo(todo, t => t.copy(editing = None), s)

    case ToggleAll =>
      val newTodos =
        if (s.todos.forall(_.done))
          s.todos.map(_.copy(done = false))
        else
          s.todos.map(_.copy(done = true))
      s.copy(todos = newTodos)

    case ClearCompleted =>
      s.copy(todos = s.todos.filterNot(_.done))

    case NoOp => s
  }

}
