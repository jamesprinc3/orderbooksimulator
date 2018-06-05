package simulator.traits

object Loggable {
  def camel2Underscore(name: String): String = {
    name.map(c => if (c.isUpper) "_" + c.toLower else c.toString).mkString("")
  }

  import scala.collection.immutable.ListMap
  import scala.reflect.runtime.universe._

  /** From: https://stackoverflow.com/a/16097409
    * Returns a map from formal parameter names to types, containing one
    * mapping for each constructor argument.  The resulting map (a ListMap)
    * preserves the order of the primary constructor's parameter list.
    */
  def caseClassParamsOf[T: TypeTag]: ListMap[String, Type] = {
    val tpe = typeOf[T]
    val constructorSymbol = tpe.decl(termNames.CONSTRUCTOR)
    val defaultConstructor =
      if (constructorSymbol.isMethod) constructorSymbol.asMethod
      else {
        val ctors = constructorSymbol.asTerm.alternatives
        ctors.map(_.asMethod).find(_.isPrimaryConstructor).get
      }

    ListMap[String, Type]() ++ defaultConstructor.paramLists
      .reduceLeft(_ ++ _)
      .map { sym =>
        sym.name.toString -> tpe.member(sym.name).asMethod.returnType
      }
  }
}

trait Loggable {

  implicit val mirror = scala.reflect.runtime.currentMirror

  def toCsvString: Seq[String] = {
    Seq()
  }

  def getCsvHeader: Seq[String] = {
    Seq()
  }
}
