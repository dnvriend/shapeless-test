import shapeless._
import shapeless.syntax.singleton._
import shapeless.labelled._

// a type
trait Cherries

// takes a literal value rather than a type
val numCherries = "numCherries" ->> 123

// takes the tag as a type rather than a literal value
field[Cherries](123)


def getFieldName[K, V](value: FieldType[K, V])(implicit witness: Witness.Aux[K]): K =
  witness.value

getFieldName(numCherries)

def getFieldValue[K, V](value: FieldType[K, V]): V = value

getFieldValue(numCherries)

// Records
// An HList of tagged elements, which is a data structure that has some of the properties
// of a Map. We can reference fields by tag, manipulate and replace them, and maintain all
// of the type and naming information along the way.

val garfield = ("cat" ->> "Garfield") :: ("orange" ->> true) :: HNil
val garfield2 = ('cat ->> "Garfield") :: ('orange ->> true) :: HNil

//

sealed trait JsValue
case class JsObj(fields: List[(String, JsValue)]) extends JsValue
case class JsArr(items: List[JsValue]) extends JsValue
case class JsString(value: String) extends JsValue
case class JsNumber(value: Double) extends JsValue
case class JsBool(value: Boolean) extends JsValue
case object JsNull extends JsValue

trait JsonEncoder[A] {
  def encode(value: A): JsValue
}

object JsonEncoder {
  def apply[A](implicit enc: JsonEncoder[A]): JsonEncoder[A] = enc
  def instance[A](f: A => JsValue): JsonEncoder[A] = (value: A) => f(value)
}

// primitives
implicit val strEnc = JsonEncoder.instance[String](str => JsString(str))
implicit val dblEnc = JsonEncoder.instance[Double](num => JsNumber(num))
implicit val intEnc = JsonEncoder.instance[Int](num => JsNumber(num))
implicit val boolEnc = JsonEncoder.instance[Boolean](bool => JsBool(bool))

// combinators
implicit def listEncoder[A](implicit enc: JsonEncoder[A]): JsonEncoder[List[A]] =
  JsonEncoder.instance[List[A]](xs => JsArr(xs.map(enc.encode)))

implicit def optionEncoder[A](implicit enc: JsonEncoder[A]): JsonEncoder[Option[A]] =
  JsonEncoder.instance[Option[A]](opt => opt.map(enc.encode).getOrElse(JsNull))

case class IceCream