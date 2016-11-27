import enumeratum.{Enum, EnumEntry}

package object todomvc {

  case class Task(txt: String, done: Boolean, editing: Option[String]) {
    //tasks with same contents can be different tasks
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

  def getFilterFn(f: Filter): Task => Boolean = f match {
    case Filter.All =>
      _ =>
        true
    case Filter.Done => _.done
    case Filter.Undone => !_.done
  }

  case class State(tasks: Seq[Task])

  sealed trait TodoListAction

  sealed trait TodoItemAction extends TodoListAction

  case class Toggle(task: Task) extends TodoItemAction
  case class Delete(task: Task) extends TodoItemAction
  case class StartEditing(task: Task) extends TodoItemAction
  case class UpdateEditing(task: Task, newTxt: String) extends TodoItemAction
  case class ConfirmEditing(task: Task) extends TodoItemAction
  case class CancelEditing(task: Task) extends TodoItemAction

  case class Add(txt: String) extends TodoListAction
  case object ToggleAll extends TodoListAction
  case object ClearCompleted extends TodoListAction
  case object NoOp extends TodoListAction

  private def updateTask(task: Task, update: Task => Task, s: State): State = {
    val newTasks = s.tasks.map(t => if (t == task) update(t) else t)
    s.copy(tasks = newTasks)
  }

  def update(a: TodoListAction, s: State): State = a match {
    case Add(txt) =>
      val newTask = Task(txt, done = false, editing = None)
      s.copy(tasks = newTask +: s.tasks)

    case Toggle(task) =>
      updateTask(task, t => t.copy(done = !t.done), s)

    case Delete(task) =>
      val newTasks = s.tasks.filterNot(_ == task)
      s.copy(tasks = newTasks)

    case StartEditing(task) =>
      updateTask(task, t => t.copy(editing = Some(t.txt)), s)

    case UpdateEditing(task, newTxt) =>
      updateTask(task, t => t.copy(editing = Some(newTxt)), s)

    case ConfirmEditing(task) =>
      updateTask(task,
                 t => t.copy(txt = t.editing.getOrElse(t.txt), editing = None),
                 s)

    case CancelEditing(task) =>
      updateTask(task, t => t.copy(editing = None), s)

    case ToggleAll =>
      val newTasks =
        if (s.tasks.forall(_.done))
          s.tasks.map(_.copy(done = false))
        else
          s.tasks.map(_.copy(done = true))
      s.copy(tasks = newTasks)

    case ClearCompleted =>
      s.copy(tasks = s.tasks.filterNot(_.done))

    case NoOp => s
  }

}
