/*
 * Copyright 2017 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend.tuples

import com.github.dnvriend.TestSpec
import shapeless._
import shapeless.syntax.std.tuple._

class TuplesTest extends TestSpec {
  final val Tuple: (Int, String, Boolean) = (23, "foo", true)

  "scala" should "support some tuple operations" in {
    // standard Scala operations
    Tuple.productArity shouldBe 3
    // note that productElement looses type information
    // because it returns 'Any'
    Tuple.productElement(0) shouldBe 23
    Tuple.productElement(1) shouldBe "foo"
    Tuple.productElement(2) shouldBe true
    Tuple.productPrefix shouldBe "Tuple3"
    // note that the productIterator is an Iterator of Any
    // thus the List is a List[Any]
    Tuple.productIterator.toList shouldBe List(23, "foo", true)

    // of course we have the arity selectors
    // these are type safe, so ._1 is of type Int, and so on
    Tuple._1 shouldBe 23
    Tuple._2 shouldBe "foo"
    Tuple._3 shouldBe true
  }

  "shapeless" should "allow tuples to be manipulated like HLists" in {
    // provided by importing shapeless.syntax.std.tuple._
    Tuple.head shouldBe a[Integer]
    Tuple.head shouldBe 23
    Tuple.tail shouldBe "foo" -> true

    Tuple.take(2) shouldBe 23 -> "foo"
    Tuple.drop(1) shouldBe "foo" -> true
    Tuple.drop(2) shouldBe Tuple1(true)
    Tuple.drop(3) shouldBe ()

    Tuple.split(1) shouldBe (Tuple1(23), "foo" -> true)

    // prepend, append, concatenate
    23 +: ("foo" -> true) shouldBe Tuple
    (23 -> "foo") :+ true shouldBe Tuple
    (23 -> "foo") ++ (true -> 2.0) shouldBe (23, "foo", true, 2.0)

    // tuple should be converted to an HList
    Tuple.productElements shouldBe 23 :: "foo" :: true :: HNil

    // tuple should be converted to an ordinary list
    // note that the list is of type 'List[Any]'
    Tuple.toList shouldBe List(23, "foo", true)
  }

  it should "map and flatMap" in {
    // Morphisms between objects of a single category are the 'normal functions' eg. val f = (_: Int) + 1
    // Functors are morphisms between categories eg: Option(1).map(f)
    // If 'f' would be a function of shape Int => Option[Int], we would get an Endofunctor (Option[Option[]) as the result
    // and we need a 'Natural Transformation' to remove that 'functor-in-functor' part, the Natural Transformation
    //
    // Natural Transformations are morphisms between functors, we could call it join as we are basically joining functors together
    // but Scala calls it flatten. As 'flatten' and 'map' are very common, there is a function called 'flatMap' that does map and flatten
    // in one go.
    //
    // Shapeless defines the '~>' operator that is a 'Natural Transformation' which joins functors together
    import shapeless.poly._
    // '~>' is a Natural Transformation

    object option extends (Id ~> Option) {
      def apply[T](t: T) = Option(t)
    }

    // map every element of the Tuple to an Option
    Tuple.map(option) shouldBe (Option(23), Option("foo"), Option(true))

    (23 -> "foo", (), true -> 2.0).flatMap(identity) shouldBe (23, "foo", true, 2.0)

    // should fold a tuple
    //    (23, "foo", (42, "answer")).foldLeft(0) {}
  }
}
