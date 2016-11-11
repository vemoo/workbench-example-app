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
}
