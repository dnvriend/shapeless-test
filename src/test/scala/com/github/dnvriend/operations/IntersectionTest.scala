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

package com.github.dnvriend.operations

import com.github.dnvriend.TestSpec
import shapeless.labelled.{ FieldType, KeyTag }
import shapeless.{ ::, HNil, HList, LabelledGeneric, Generic }
import shapeless.ops.hlist.{ Align, Diff, Intersection, Union }
import shapeless.syntax.SingletonOps
import shapeless.syntax.singleton._

case class Person(name: String, age: Int)
object Person {
  val Empty = Person("fn", 42)
}
case class Person2(name: String, lastName: String, age: Int)
object Person2 {
  val Empty = Person2("fn", "ln", 42)
}

class IntersectionTest extends TestSpec {
  "intersection" should "be defined as" in {
    val a = Set(1, 2, 3)
    val b = Set(2, 3)
    a.intersect(b) shouldBe Set(2, 3)
  }

  it should "hlist intersect" in {
    val g1: String :: Int :: Boolean :: HNil = "hello" :: 1 :: true :: HNil
    val g2 = Intersection[String :: Int :: Boolean :: HNil, String :: Boolean :: HNil].apply(g1)
    g2 shouldBe "hello" :: true :: HNil
  }

  it should "use generic doIntersect" in {
    def doIntersect[A, B, ARepr <: HList, BRepr <: HList](a: A, b: B)(implicit
      genA: Generic.Aux[A, ARepr],
      genB: Generic.Aux[B, BRepr],
      intersect: Intersection[ARepr, BRepr]) = {
      intersect(genA.to(a))
    }

    // which types intersect of these two generic representations?
    doIntersect(Person.Empty, Person2.Empty) shouldBe "fn" :: 42 :: HNil

    doIntersect(Person2.Empty, Person.Empty) shouldBe "fn" :: 42 :: HNil
  }

  "difference" should "be defined as" in {
    val a = Set(1, 2, 3)
    val b = Set(2, 3)
    a.diff(b) shouldBe Set(1)
  }

  it should "hlist difference" in {
    val g1: String :: Int :: Boolean :: HNil = "hello" :: 1 :: true :: HNil
    val g2 = Diff[String :: Int :: Boolean :: HNil, String :: Boolean :: HNil].apply(g1)
    g2 shouldBe 1 :: HNil
  }

  it should "use generic doDiff" in {
    def doDiff[A, B, ARepr <: HList, BRepr <: HList](a: A, b: B)(implicit
      genA: Generic.Aux[A, ARepr],
      genB: Generic.Aux[B, BRepr],
      diff: Diff[ARepr, BRepr]) = {
      diff(genA.to(a))
    }

    // what is in Person2 that is not in Person?
    doDiff(Person2.Empty, Person.Empty) shouldBe "ln" :: HNil

    // what is in Person that is not in Person2?
    doDiff(Person.Empty, Person2.Empty) shouldBe HNil
  }

  "union" should "be defined as" in {
    val a = Set(1, 2, 3)
    val b = Set(2, 3, 4)
    a.union(b) shouldBe Set(1, 2, 3, 4)
  }

  it should "use generic doUnion" in {
    def doUnion[A, B, ARepr <: HList, BRepr <: HList](a: A, b: B)(implicit
      genA: Generic.Aux[A, ARepr],
      genB: Generic.Aux[B, BRepr],
      union: Union[ARepr, BRepr]) = {
      union(genA.to(a), genB.to(b))
    }

    doUnion(Person.Empty, Person2.Empty) shouldBe "fn" :: 42 :: "ln" :: HNil
  }

  it should "hlist union" in {
    val g1 = "hello" :: 1 :: HNil
    val g2 = "hello" :: true :: HNil
    val g3 = Union[String :: Int :: HNil, String :: Boolean :: HNil].apply(g1, g2)
    g3 shouldBe "hello" :: 1 :: true :: HNil
  }

  it should "Align HList1 to HList2" in {
    // Type class supporting permuting this `HList` into the same order as another `HList` with
    // the same element types.
    val g1: String :: Int :: Boolean :: HNil = "hello" :: 1 :: true :: HNil
    val g2 = Align[String :: Int :: Boolean :: HNil, Boolean :: Int :: String :: HNil].apply(g1)
    g2 shouldBe true :: 1 :: "hello" :: HNil
  }

  it should "labelled gen" in {
    val g1 = {
      'name ->> "fn ln" ::
        'age ->> 42 ::
        HNil
    }

    val g2 = {
      'name ->> "fn" ::
        'lastName ->> "ln" ::
        'age ->> 42 ::
        HNil
    }

    LabelledGeneric[Person].from(g1) shouldBe Person("fn ln", 42)
    LabelledGeneric[Person2].from(g2) shouldBe Person2("fn", "ln", 42)
  }
}
