import enumeratum.{Enum, EnumEntry}

package object todomvc {

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

  sealed trait TodoAction
  case class Add(txt: String) extends TodoAction
  case class Toggle(task: Task) extends TodoAction
  case class Delete(task: Task) extends TodoAction
  case class SetEditing(task: Task) extends TodoAction
  case class UpdateEditingText(newTxt: String) extends TodoAction
  case object ConfirmEditing extends TodoAction
  case object CancelEditing extends TodoAction
  case object ToggleAll extends TodoAction
  case object ClearCompleted extends TodoAction
  case object NoOp extends TodoAction

  def update(a: TodoAction, s: State): State = a match {
    case Add(txt) =>
      val newId = if (s.tasks.isEmpty) 1 else s.tasks.map(_.id).max + 1
      val newTask = Task(newId, txt, done = false)
      s.copy(tasks = newTask +: s.tasks)

    case Toggle(task) =>
      val newTasks = s.tasks.map { t =>
        if (t == task)
          t.copy(done = !t.done)
        else t
      }
      s.copy(tasks = newTasks)

    case Delete(task) =>
      val newTasks = s.tasks.filterNot(_ == task)
      s.copy(tasks = newTasks)

    case SetEditing(task) =>
      s.copy(editing = Some(task))

    case UpdateEditingText(txt) =>
      val updEditingTask = s.editing.map(_.copy(txt = txt))
      s.copy(editing = updEditingTask)

    case ConfirmEditing => s.editing match {
      case Some(editing) =>
        s.copy(
          tasks = s.tasks.map(t => if (t == editing) editing else t),
          editing = None
        )
      case None => s
    }

    case CancelEditing =>
      s.copy(editing = None)

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
