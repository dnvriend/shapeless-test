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

package com.github.dnvriend.derivation

import com.github.dnvriend.TestSpec
import SimpleDerivationTest._

object SimpleDerivationTest {
  case class Person(name: String, age: Int)
  object Person {
    val Empty = Person("name", 42)
    implicit val enc = CsvEncoder.instance[Person](p => List(p.name, p.age.toString))
  }

  case class Address(street: String, zipcode: String)
  object Address {
    val Empty = Address("street", "zipcode")
    implicit val enc = CsvEncoder.instance[Address](addr => List(addr.street, addr.zipcode))
  }

  trait CsvEncoder[A] {
    def encode(value: A): List[String]
  }

  object CsvEncoder {
    def apply[A](implicit enc: CsvEncoder[A]): CsvEncoder[A] = enc
    def instance[A](f: A => List[String]): CsvEncoder[A] = {
      new CsvEncoder[A] {
        override def encode(value: A): List[String] = f(value)
      }
    }
  }

  // derives a CsvEncoder for a pair (A, B) for the types A and B
  // it will be used by the compiler as a skeleton to derive type classes for the pairs (A, B)
  implicit def pairCsvEncoder[A, B](implicit encA: CsvEncoder[A], encB: CsvEncoder[B]): CsvEncoder[(A, B)] = {
    CsvEncoder.instance[(A, B)] {
      case (a, b) => encA.encode(a) ++ encB.encode(b)
    }
  }
}

class SimpleDerivationTest extends TestSpec {
  it should "summon the CsvEncoder for Person" in {
    CsvEncoder[Person].encode(Person.Empty) shouldBe List("name", "42")
  }

  it should "summon the CsvEncoder for Address" in {
    CsvEncoder[Address].encode(Address.Empty) shouldBe List("street", "zipcode")
  }

  //////////////////
  //
  // in scala the compiler can derive new
  // type classes when the following is true:
  //
  // - an implicit def can be used when
  // - all the parameters of the implicit def are themselves marked as implicit
  // - the result is a new typeclass
  //
  /////////////////

  it should "derive a new typeclass, CsvEncoder[(Person, Person)]" in {
    CsvEncoder[(Person, Person)].encode((Person.Empty, Person.Empty)) shouldBe List("name", "42", "name", "42")
  }

  it should "derive a new typeclass, CsvEncoder[(Person, Address)]" in {
    CsvEncoder[(Person, Address)].encode((Person.Empty, Address.Empty)) shouldBe List("name", "42", "street", "zipcode")
  }

  it should "derive a new typeclass, CsvEncoder[(Address, Person)]" in {
    CsvEncoder[(Address, Person)].encode((Address.Empty, Person.Empty)) shouldBe List("street", "zipcode", "name", "42")
  }

  it should "derive a new typeclass, CsvEncoder[(Address, Address)]" in {
    CsvEncoder[(Address, Address)].encode((Address.Empty, Address.Empty)) shouldBe List("street", "zipcode", "street", "zipcode")
  }
}
