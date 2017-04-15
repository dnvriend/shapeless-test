# shapeless-test
A small study project on [shapeless](https://github.com/milessabin/shapeless), a generic programming library for Scala.

## TL;DR
Shapeless gives us a way to use friendly semantic types - sealed traits for the coproducts and case classes for the products - and a generic representation when we need interoperability. To convert from/to these generic representation Shapeless provides the HList abstraction to convert products and coproducts (case classes, sealed traits and tuples) to the generic HList representation and provides primitives to manipulate the HList where appropriate.

## Introduction
Shapeless provides automatic type class derivation by means of the Heterogeneous List or 'HList' abstraction.

## Inductive Based Programming


## HList
An HList is either the empty list `HNil` or a pair called `HList-cons` or just `cons` ::[H, T],
and because you can write a parameterized type that has a binary arity using an infix notation,
you can also write the pair as `H :: T`. The `H` is an arbitrary type and `T` is another HList.

Because every cons has its own H and T, the type of each element is encoded separately in the type of the overall List.

The HList types are available in the following package:

```scala
import shapeless.{HList, ::, HNil}

val product: String :: Int :: Boolean :: HNil = "Sunday" :: 1 :: false :: HNil
```

## Generic
Shapeless provides a type class called `shapeless.Generic` that allows us to switch back and forth between a concrete
ADT and its generic representation.

```scala
scala> import shapeless.Generic
import shapeless.Generic

scala> case class Person(name: String, age: Int)
defined class Person

scala> val dennis = Person("Dennis", 42)
dennis: Person = Person(Dennis,42)

scala> Generic[Person].to(dennis)
res0: shapeless.::[String,shapeless.::[Int,shapeless.HNil]] = Dennis :: 42 :: HNil
```

You can also switch from the generic representation back to the product type:

```scala
scala> Generic[Person].from(gen)
res1: Person = Person(Dennis,42)
```

## Encoding coproducts
Coproducts can be encoded as follows:

```scala
import shapeless.{Coproduct, :+:, CNil, Inl, Inr}
case object A
case object B
case object C

type Choice = A.type :+: B.type :+: C.type :+: CNil

scala> val a: Choice = Inl(A)
a: Choice = Inl(A)

scala> val b: Choice = Inr(Inl(B))
b: Choice = Inr(Inl(B))

scala> val c: Choice = Inr(Inr(Inl(C)))
c: Choice = Inr(Inr(Inl(C)))
```

We can use Generic to encode coproducts as well:

```scala
sealed trait Animal
case class Bird(legs: Int, age: Int) extends Animal
case class Horse(legs: Int, age: Int) extends Animal

// we need to create a generic from the sealed trait
val animalGen = Generic[Animal] // Bird :+: Horse :+: CNil
```

## Implicit resolution
Implicit resolution is a search process. The compiler uses heuristics to determine whether the search process is
converging on a solution. If the heuristics don't yield favorable results for a particular branch of search
the comiler assumes the branch is not converging and moves onto another.

One heuristic is specifically designed to avoid infinite loops. If the compiler sees the same target type twice
in a particular branch of search, it gives up and moves on.

```
A heuristic, is a function that ranks alternatives in search algorithms at each branching step based on available information
to decide which branch to follow.
```

## Implicit resolution rules
The implicits available under number 1 below has precedence over the ones under number 2.

If there are several eligible arguments which match the implicit parameter’s type, a most specific one will be chosen
using the rules of static overloading resolution (see Scala Specification §6.26.3).

1. First look in current scope
  - Implicits defined in current scope
  - Explicit imports
  - wildcard imports

2. Now look at associated types in
  - Companion objects of a type
  - Implicit scope of an argument's type (2.9.1)
  - Implicit scope of type arguments (2.8.0)
  - Outer objects for nested types

## Implicit parameters
When using shapeless we most often use implicit parameters on an implicit method, where all parameters are themselves
marked implicit. The compiler resolves these implicits from left to right, backtracking if it can't find a working
combination. We should write the implicits in the order we need them, using one or more type variables to
connect them to the previous implicits.

In shapeless we use dependent typing to find the target type that depends on values in our code. We use the Scala
implicit search to conventiently resolve intermediate and target types given a starting point at the call-site.

We often have to use multiple steps to calculate a result eg. using a Generic toget a Repr, and then another type class
to get to another type and so on.

This 'sequence of steps' will be expressed by the implicit parameters in our method and these have to be in the order
we need them to resolve to the target type.

The compiler can only solve for one constraint at a time, so we musn't over-constraint a single implicit.

We should state the return type explicitly, specifying any type parameters and type members that may be needed elsewhere.
Type members are often important, so we should use Aux types to preserve them where appropiarate.

If we don't state type members in the return type, they won't be available to the compiler for further implicit resolution.

The Aux type alias pattern is useful for keeping out code readable. We should look out for Aux aliases when using tools
from the shapeless toolbox, and implement Aux aliases for our own dependently typed functions.

When we find a useful chain of dependently typed operations, we can capure them as a single type class. This is
called the 'lemma' pattern - a term borrowed from mathematical proofs.

## Debugging missing implicits using implicitly
Debugging missing implicits is a manual process. The compiler isn't giving us enough information so we must know
how the implicit resolution and expansion works for our generated derived type class. When we are missing
a simple CsvEncoder for a type Float for example, and the compiler needs it to derive a type class it just
gives up with the message that it cannot find an implicit value for the full type. Its up to us to dig down the
implicit resolution hierarchy to find out what is wrong.

```scala
scala> case class Foo(bar: String, baz: Float)
defined class Foo

scala> implicitly[CsvEncoder[Foo]]
<console>:33: error: could not find implicit value for parameter e: CsvEncoder[Foo]
       implicitly[CsvEncoder[Foo]]

scala> implicitly[CsvEncoder[String :: Float :: HNil]]
<console>:34: error: could not find implicit value for parameter e: CsvEncoder[shapeless.::[String,shapeless.::[Float,shapeless.HNil]]]
       implicitly[CsvEncoder[String :: Float :: HNil]]

// hmm, okay do we have a CsvEncoder for Float?
scala> implicitly[CsvEncoder[Float]]
<console>:34: error: could not find implicit value for parameter e: CsvEncoder[Float]
       implicitly[CsvEncoder[Float]]

// no, lets create one
scala> implicit val floatEnc = new CsvEncoder[Float] { def encode(a: Float) = List(a.toString) }
floatEnc: CsvEncoder[Float] = $anon$1@529e745

// does it work now?
scala> implicitly[CsvEncoder[Foo]]
res0: CsvEncoder[Foo] = $anon$1@34f96e4b

// yes!
```

## Debugging missing implicits using reify
The `reify` method from `scala.reflect` takes a Scala expression as a parameter and returns an AST object
representing the expression tree, complete with type annotations:

```scala
import scala.reflect.runtime.universe._

scala> implicit val hnilEncoder = new CsvEncoder[HNil] { def encode(a: HNil)  = Nil }
hnilEncoder: CsvEncoder[shapeless.HNil]{def encode(a: shapeless.HNil): scala.collection.immutable.Nil.type} = $anon$1@21094ad9

scala> implicit val strEncoder = new CsvEncoder[String] { def encode(a: String) = List(a) }
strEncoder: CsvEncoder[String] = $anon$1@5102385a

scala> implicit val intEncoder = new CsvEncoder[Int] { def encode(a: Int) = List(a.toString) }
intEncoder: CsvEncoder[Int] = $anon$1@1415980d

scala> implicit def genericEncoder[A, R](implicit gen: Generic.Aux[A, R], enc: CsvEncoder[R]): CsvEncoder[A] = new CsvEncoder[A] { println(s"genenc"); def encode(value: A) = enc.encode(gen.to(value)) }
genericEncoder: [A, R](implicit gen: shapeless.Generic.Aux[A,R], implicit enc: CsvEncoder[R])CsvEncoder[A]

scala> reify(implicitly[CsvEncoder[Int]])
res0: reflect.runtime.universe.Expr[CsvEncoder[Int]] = Expr[CsvEncoder[Int]](Predef.implicitly[$read.CsvEncoder[Int]]($read.intEncoder))

scala> reify(implicitly[CsvEncoder[String]])
res1: reflect.runtime.universe.Expr[CsvEncoder[String]] = Expr[CsvEncoder[String]](Predef.implicitly[$read.CsvEncoder[Predef.String]]($read.strEncoder))

scala> reify(implicitly[CsvEncoder[Person]])
res2: reflect.runtime.universe.Expr[CsvEncoder[Person]] = Expr[CsvEncoder[Person]](Predef.implicitly[$read.CsvEncoder[$read.Person]]($read.genericEncoder(Generic.materialize, $read.hlistEncoder($read.strEncoder, $read.hlistEncoder($read.intEncoder, $read.hnilEncoder)))))
```

## Generalized Type Constraints
The operators `<:<`, `<%<` and `=:=` are called 'generalized type constraints` They allow you, from within a
type-parameterized class or trait, to further constrain one of its type parameters.

```scala
scala> def proof[T](implicit ev: T): Boolean = true
proof: [T](implicit ev: T)Boolean

scala> proof[Null <:< String]
res0: Boolean = true

scala> proof[Int =:= Int]
res1: Boolean = true

scala> trait Foo { type Out }
defined trait Foo

scala> val foo = new Foo { type Out = Int }
foo: Foo{type Out = Int} = $anon$1@96a75da

scala> proof[foo.Out =:= Int]
res2: Boolean = true
```

## Dependent Types
A dependent type is a type that depends on a value. Usually in programming languages, the type and the value world
are totally separated and we use the type only to constraint and give information about the values. With dependent types
there is no separation between the two worlds and we get new powerful features:

- we have types that depend on values, which means that we can compute them in a similar way to values, this gives us more flexibility
- we can define stronger constraints for the values


When using shapeless, we are trying to find a target type that depends on values in our code. This relationship is
called dependent typing. Dependently typed functions provide a means of calculating one __type__ from another.
Think of it as computations at the type-level. We can chain dependently typed functions to perform calculations
involving multiple steps. For example, we should be able to use a Generic to calculate a Repr __type__ for a case class
and use a Last to calculate the __type__ of the last element.

## Singleton Types
Singleton types are types that exclusively belong to one value with that type. For example:

```scala
scala> object Foo
defined object Foo

scala> Foo
res1: Foo.type = Foo$@760602d

scala> def doFoo(x: Foo.type): String = x.toString
doFoo: (x: Foo.type)String

scala> doFoo(Foo)
res2: String = Foo$@760602d
```

There is only one type of object Foo and that is Foo.type.

## Literal Types
Singleton types applied to literal values are called Literal Types and are available in the upcoming Scala 2.12.2 and
of course in __typelevel__ scala 2.11.8 and 2.11.1.

```scala
// enable in build.sbt
scalacOptions in ThisBuild += "-Yliteral-types"

// then you can use it
scala> val x: 42 = 42
x: 42 = 42
```

But if you are not using typelevel-scala then Shapeless got you covered because it provides a `narrow` macro that
converts a literal expression to a singleton-typed literal expression:

```scala
import shapeless.syntax.singleton._

scala> 42.narrow
res0: Int(42) = 42
```

Shapeless uses literal types to model the names of fields in case classes so Shapeless knows at compile time what
the type is of the field. It does this by tagging the types of the fields with the literal types of their names.

```scala
val number = 42
```

The field 'number' is an Int in two worlds, at runtime, where it has the actual value 42 and at compile-time, where
the compiler uses the type to calculate which pieces of code work together and to search for implicits.

## Phantom Types
Phantom types are types with no run-time sementics and are used eg. by Shapeless to tag fields. For example

```scala
scala> val number = 42
number: Int = 42

// the phantom type
scala> trait Cherries
defined trait Cherries
             
scala> val numCherries = number.asInstanceOf[Int with Cherries]
numCherries: Int with Cherries = 42
```

Shapeless uses this trick to tag fields and subtypes in an ADT with the singleton types of their names. Shapeless
provides the syntax `->>` to tag the expression on the right side of the arrow with the singleton type of 
the literal expression on the left:

```scala
scala> import shapeless.labelled._
import shapeless.labelled._

scala> import shapeless.syntax.singleton._
import shapeless.syntax.singleton._

scala> val number = 42
number: Int = 42

scala> val numCherries = "numCherries" ->> number
numCherries: Int with shapeless.labelled.KeyTag[String("numCherries"),Int] = 42
```

What we've done is tag `number` with the Phantom Type `KeyTag["numCherries", Int]`. The tag encodes both 
__the name and type of the field__ which is useful when searching for entries in a Repr using implicit resolution.


## Tagged Types

## HList Type classes



- Intersection
An intersection is the set that contains all the elements of set A that also belongs to set B.
Intersection is a type class supporting `HList` intersection. In case of duplicate types, this operation 
is a multiset intersection. If type `T` appears n times in this `HList` and m < n times in `M`, the resulting `HList` 
contains the first m elements of type `T` in this `HList`.

Also available if `M` contains types absent in this `HList`.

## Projects
- [Kittens -  a Scala library which provides instances of type classes from the Cats library for ADTs using shapeless - Miles Sabin](https://github.com/milessabin/kittens)
- [export-hook -  infrastructure for type class providers and other orphan instances to implicit scope - Miles Sabin](https://github.com/milessabin/export-hook)

## Video
- [The Type Astronaut's Guide to Shapeless — Dave Gurnell](https://www.youtube.com/watch?v=Zt6LjUnOcFQ)
- [Generic Derivation: The Hard Parts — Travis Brown](https://www.youtube.com/watch?v=80h3hZidSeE)
- [Kittens: Shapeless Typeclass Derivation for Cats — Miles Sabin](https://www.youtube.com/watch?v=fM7x2oWSM70)

## Resources
- [Example code to accompany shapeless-guide](https://github.com/underscoreio/shapeless-guide-code)
- [Shapeless: not a tutorial - part 1](http://kanaka.io/blog/2015/11/09/shapeless-not-a-tutorial-part-1.html)
- [Shapeless: not a tutorial - part 2](http://kanaka.io/blog/2015/11/10/shapeless-not-a-tutorial-part-2.html)
- [Getting started with Shapeless - Mike Limansky](http://limansky.me/posts/2016-11-24-getting-started-with-shapeless.html)
- [Type classes and generic derivation - Travis Brown](https://meta.plasm.us/posts/2015/11/08/type-classes-and-generic-derivation/)
- [Type level sorting in Shapeless - Miles Sabin](https://milessabin.com/blog/2012/01/27/type-level-sorting-in-shapeless/)
- [Type all the things - Julien Tournay](http://jto.github.io/articles/type-all-the-things/)
- [Getting started with Shapeless - Julien Tournay](http://jto.github.io/articles/getting-started-with-shapeless/)
- [Boilerplate free conversion from case classes to shapeless records via LabelledGeneric - Miles Sabin](https://gist.github.com/milessabin/9042788)
- [Gist of Miles Sabin](https://gist.github.com/milessabin)
- [Dependent Types - Type Level Programming in Scala step by step](http://gigiigig.github.io/tlp-step-by-step/dependent-types.html)
- [Dependent Types - some tips, tricks and techniques](http://wheaties.github.io/Presentations/Scala-Dep-Types/dependent-types.html#/)
- [Henkan - A domain converter using shapeless](https://github.com/kailuowang/henkan)