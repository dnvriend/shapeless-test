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

package com.github.dnvriend.product

import com.github.dnvriend.TestSpec
import com.github.dnvriend.product.LabelGenTest._
import shapeless._
import shapeless.record._
import shapeless.syntax._

object LabelGenTest {
  case class Car(name: String, make: Int)
  object Car {
    val Empty = Car("Ford", 1968)
    implicit val labelledGen = LabelledGeneric[Car]
    implicit val gen = Generic[Car]
  }
}

class LabelGenTest extends TestSpec {
  it should "encode to gen" in {
    LabelledGeneric[Car].to(Car.Empty) shouldBe "Ford" :: 1968 :: HNil
  }

  it should "create a labelled generic and process the generated record" in {
    // symbols vs strings
    // symbols are used when you need identifiers to point to something.
    // For example below we need to identify the label 'name', so we need
    // a symbol to identify the field of the case class we want the value of
    //
    // why not use strings? Well, that has a technical reason, strings can be
    // used as identifiers, but only if you know that the runtime system has
    // converted for example the following two Strings "name" and "name" to
    // be exactly the same object, a process called 'interning'. As a programmer
    // you don't know, but with scala.Symbol you can be assured that two symbols
    // like 'name and 'name are exactly the same object
    //
    // from the scala docs:
    // scala.Symbol provides a simple way to get unique objects for equal strings.
    // Since symbols are interned, they can be compared using reference equality which is fast.
    // Instances of 'Symbol' can be created easily with Scala's built-in single quote
    // mechanism so 'name for example.
    //

    // carRecord is a record because it has the field names encoded
    //    val carRecord: Car.labelledGen.Repr = LabelledGeneric[Car].to(Car.Empty)
    val carRecord = LabelledGeneric[Car].to(Car.Empty)
    // shapeless.::[String with shapeless.labelled.KeyTag[Symbol with shapeless.tag.Tagged[String("name")],String],shapeless.::[Int with shapeless.labelled.KeyTag[Symbol with shapeless.tag.Tagged[String("age")],Int],shapeless.HNil]] = name :: 42 :: HNil

    // carRecord should be converted back to a Car
    LabelledGeneric[Car].from(carRecord) shouldBe Car.Empty

    // get the keys in an HList (note the symbols)
    carRecord.keys shouldBe 'name :: 'make :: HNil

    // get the values in an HList
    carRecord.values shouldBe "Ford" :: 1968 :: HNil

    // get the field 'name
    carRecord.get('name) shouldBe "Ford"
    carRecord('name) shouldBe "Ford"

    // get the field 'make
    carRecord.get('make) shouldBe 1968
    carRecord('make) shouldBe 1968
  }
}
