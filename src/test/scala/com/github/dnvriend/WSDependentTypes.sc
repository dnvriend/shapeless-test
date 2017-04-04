import shapeless._
import shapeless.ops.hlist._

// dependently typed functions

trait Second[L <: HList] {
  type Out
  def apply(value: L): Out
}

object Second {
  // Aux type alias or Aux pattern
  // the Aux pattern allows us to bind the abstract type member
  // to a type variable that we constrain as we see fit.

  // we use the Aux type in the companion object beside the
  // standard apply 'summoner' method
  type Aux[L <: HList, O] = Second[L] { type Out = O } // type refinement
  def apply[L <: HList](implicit sec: Second[L]): Aux[L, sec.Out] = sec

  // the return type is Aux[L, O] and not Second[L]. Using Aux ensures that
  // the apply-method does not erase the type members on summoned instances.
  // IF we define the return type as Second[L], the Out-type member will be erased
  // from the return type and the type class will not work correctly.
}

implicitly[Last[String :: Int :: HNil]]

val l = Last[String :: Int :: HNil]
l("foo" :: 42 :: HNil)

the[Last[String :: Int :: HNil]]

trait Encoder[A] {
  type Out
  def encode(value: A): Out
}

trait ListEncoder[A, B] extends Encoder[A] {
  type Out = List[B]
}

trait StrListEncoder[A] extends ListEncoder[A, String]

object StrListEncoder {
  def instance[A](f: A => List[String]): StrListEncoder[A] = new StrListEncoder[A] {
    override def encode(value: A): Out = f(value)
  }
}

implicit val intEncoder = StrListEncoder.instance[Int](x => List(x.toString))

the[StrListEncoder[Int]].encode(1)

implicit def hlistSecond[A, B, Rest <: HList]: Second.Aux[A :: B :: Rest, B] = {
  new Second[A :: B :: Rest] {
    type Out = B
    def apply(value: A :: B :: Rest): B = value.tail.head
  }
}

val second1 = Second[String :: Boolean :: Int :: HNil]
val second2 = Second[String :: Int :: Boolean :: HNil]

second1("hello" :: true :: 1 :: HNil)
second2("hello" :: 42 :: true :: HNil)

def proof[T](implicit ev: T) = {
  println(ev.getClass.getName)
  true
}

// see: http://www.scala-lang.org/old/node/10632
// type-level checks
proof[Null <:< String]
proof[Int =:= Int]

trait Foo {
  type Out
}

val foo = new Foo { type Out = Int}

// proof that these types are the same
proof[foo.Out =:= Int]
