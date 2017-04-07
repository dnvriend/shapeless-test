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
import shapeless.{ ::, HNil, LabelledGeneric }
import shapeless.ops.hlist.{ Align, Diff, Intersection, Union }
import shapeless.syntax.SingletonOps
import shapeless.syntax.singleton._

case class Person(name: String, age: Int)
case class Person2(name: String, lastName: String, age: Int)

class IntersectionTest extends TestSpec {
  it should "intersect" in {
    val g1: String :: Int :: Boolean :: HNil = "hello" :: 1 :: true :: HNil
    val g2 = Intersection[String :: Int :: Boolean :: HNil, String :: Boolean :: HNil].apply(g1)
    g2 shouldBe "hello" :: true :: HNil
  }

  it should "Difference" in {
    val g1: String :: Int :: Boolean :: HNil = "hello" :: 1 :: true :: HNil
    val g2 = Diff[String :: Int :: Boolean :: HNil, String :: Boolean :: HNil].apply(g1)
    g2 shouldBe 1 :: HNil
  }

  it should "Align HList1 to HList2" in {
    // Type class supporting permuting this `HList` into the same order as another `HList` with
    // the same element types.
    val g1: String :: Int :: Boolean :: HNil = "hello" :: 1 :: true :: HNil
    val g2 = Align[String :: Int :: Boolean :: HNil, Boolean :: Int :: String :: HNil].apply(g1)
    g2 shouldBe true :: 1 :: "hello" :: HNil
  }

  it should "Union" in {
    val g1 = "hello" :: 1 :: HNil
    val g2 = "hello" :: true :: HNil
    val g3 = Union[String :: Int :: HNil, String :: Boolean :: HNil].apply(g1, g2)
    g3 shouldBe "hello" :: 1 :: true :: HNil
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
