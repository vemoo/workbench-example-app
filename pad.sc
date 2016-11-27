case class A(id:Int, nombre:String){
  override def equals(obj: scala.Any): Boolean = super.equals(obj)
}

val a1 = A(1, "uno")
val a2 = A(1, "uno")

a1==a1