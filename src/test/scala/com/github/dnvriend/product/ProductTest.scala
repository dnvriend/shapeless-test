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
import shapeless._
import shapeless._
import nat._
import ops.nat._
import test._
import LT._
import LTEq._

case class IceCream(name: String, numCherries: Int, inCone: Boolean)
object IceCream {
  implicit val generic = Generic[IceCream]
  final val Sundae = IceCream("Sundae", 1, false)
}

case class Employee(name: String, number: Int, manager: Boolean)
object Employee {
  implicit val generic = Generic[Employee]
}

case class Address(street: String, postcode: String)
object Address {
  implicit val gen = Generic[Address]
}
case class Person(name: String, age: Int, address: Address)
object Person {
  final val DefaultPerson = Person("Dennis", 42, Address("my-street", "90210"))
  implicit val gen = Generic[Person]
}

class ProductTest extends TestSpec {
  // The HList is the generic representation of Product types so case classes and tuples

  // The real power of HLists and Coproducts comes from their recursive structure.
  // We can write code to traverse representations and calculate values from
  // their constituent elements

  it should "convert to and from the generic representation of a product type" in {
    // the Repr type alias is the generic representation of the product type
    // the Repr is a stable reference to the type that can change of course
    // ie. when you change the product type
    val repr = Generic[IceCream].to(IceCream.Sundae)
    repr shouldBe "Sundae" :: 1 :: false :: HNil

    // the type class should convert back from the generic representation
    Generic[IceCream].from(repr) shouldBe IceCream.Sundae
  }

  it should "convert an icecream into an employee" in {
    val iceCreamRepr = Generic[IceCream].to(IceCream.Sundae)
    val iceCreamEmployee = Generic[Employee].from(iceCreamRepr)
    iceCreamEmployee shouldBe Employee("Sundae", 1, false)
  }

  it should "also work on tuples and on case classes" in {
    val tupleGen = Generic[(String, Int, Boolean)]
    val repr = tupleGen.to(("Hello", 123, true))
    val xs = "Foo" :: 543210 :: false :: HNil
    tupleGen.from(xs) shouldBe ("Foo", 543210, false)

    // the most generic representation is the HList so
    val mostGeneric = "Foo" :: 1234 :: true :: HNil

    // having an appropriate generic typeclass in scope for a certain ADT, you can
    // transform to/from the HList representation

    //    Generic.Aux[A, R]
    Generic[IceCream].from(mostGeneric) shouldBe IceCream("Foo", 1234, true)
    Generic[Employee].from(mostGeneric) shouldBe Employee("Foo", 1234, true)
    tupleGen.from(mostGeneric) shouldBe ("Foo", 1234, true)
  }

  it should "create gen for nested product types" in {
    val repr = Generic[Person].to(Person.DefaultPerson)
    Generic[Person].from(repr) shouldBe Person.DefaultPerson
  }

  it should "" in {
    val repr1 = "str" :: 1 :: false :: HNil
    val repr2 = 1 :: "str" :: false :: HNil
    val repr3 = false :: 1 :: "str" :: HNil
  }
}

