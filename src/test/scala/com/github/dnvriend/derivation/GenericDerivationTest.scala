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
import com.github.dnvriend.derivation.GenericDerivationTest._
import shapeless._

object GenericDerivationTest {
  case class Person(name: String, age: Int)
  object Person {
    val Empty = Person("name", 42)
    implicit val gen = Generic[Person] // String :: Int :: HNil
  }

  case class Address(street: String, zipcode: String)
  object Address {
    val Empty = Address("street", "zipcode")
    implicit val gen = Generic[Address] // String :: String :: HNil
  }

  trait CsvEncoder[A] {
    def encode(value: A): List[String]
  }

  object CsvEncoder {
    def apply[A](implicit enc: CsvEncoder[A]): CsvEncoder[A] = enc
    def instance[A](f: A => List[String]): CsvEncoder[A] = new CsvEncoder[A] {
      override def encode(value: A): List[String] = f(value)
    }
  }

  // like with the SimpleDerivationTest, where we derive a type class instance
  // for CsvEncoder[(A, B)] so a pair of (A, B), we will do the same but not
  // for the 'pair' type, but for a HList type. As you know, the HList type can
  // be, for example, 'String :: Int :: HNil' or 'String :: String :: HNil',
  // granted that looks a bit strange when your used to 'Int', 'String', 'Boolean'
  // for example, 'String :: Int :: HNil' is still a type though.

  // Shapeless calls a type like 'String :: Int :: HNil' the Repr, which is a type alias,
  // so we will call any 'HList-type', Repr as well.

  // So we will derive a type class for any Repr => List[String], lets try:

  // as you know, the HList is a List structure, which means it has a Head and a Tail.
  // The Head is type H and the Tail is type T, and is of type HList. The Head can be
  // of any type, like String for example.
  //
  // When the Repr is String :: Int :: HNil for example, the compiler will look for the following:
  // - A CsvEncoder[String]
  // - A CsvEncoder[Int :: HNil]
  //
  // the Repr is now Int :: HNil, so the compiler will look for
  // - A CsvEncoder[Int]
  // - A CsvEncoder[HNil]
  //
  // Basically the compiler derives two type class instances,
  // - CsvEncoder[String :: Int :: HNil] and
  // - CsvEncoder[Int :: HNil]
  //
  // The other instances the compiler cannot derive, so we have to give it to
  // the compiler, so these are the following CsvEncoders:
  // - CsvEncoder[String],
  // - CsvEncoder[Int],
  // - CsvEncoder[HNil]
  //
  // Given these types and the skeleton, it can derive everything:
  //
  implicit def hlistEncoder[H, T <: HList](implicit encH: CsvEncoder[H], encT: CsvEncoder[T]): CsvEncoder[H :: T] = {
    CsvEncoder.instance[H :: T] {
      case h :: t => encH.encode(h) ++ encT.encode(t)
    }
  }

  // the type class instances the compiler cannot derive
  // so one for CsvEncoder[HNil], CsvEncoder[String] and CsvEncoder[Int]
  implicit val hnilEncoder: CsvEncoder[HNil] = CsvEncoder.instance[HNil](_ => Nil)
  implicit val strEncoder: CsvEncoder[String] = CsvEncoder.instance[String](str => List(str))
  implicit val intEncoder: CsvEncoder[Int] = CsvEncoder.instance[Int](x => List(x.toString))

  // you would be right if you are thinking, when we have a Repr that contains a Boolean for example
  // or a java.util.Date, we need CsvEncoders for them as well...
  // but lets start simple and focus on just the following three atomic types
  // - the 'HNil' that represents the end of the HList,
  // - the String,
  // - the Int
}

class GenericDerivationTest extends TestSpec {
  it should "summon the generic for Person" in {
    Generic[Person].to(Person.Empty) shouldBe "name" :: 42 :: HNil
  }

  it should "summon the generic for Address" in {
    Generic[Address].to(Address.Empty) shouldBe "street" :: "zipcode" :: HNil
  }

  it should "derive a type class instance for Repr 'String :: Int :: HNil'" in {
    CsvEncoder[String :: Int :: HNil].encode("name" :: 42 :: HNil) shouldBe List("name", "42")
  }

  // you can derive any type class instance you need
  it should "derive a type class instance for Repr 'Int :: Int :: HNil'" in {
    CsvEncoder[Int :: Int :: HNil].encode(42 :: 42 :: HNil) shouldBe List("42", "42")
  }

  it should "derive a type class instance for Repr 'String :: String :: HNil'" in {
    CsvEncoder[String :: String :: HNil].encode("name" :: "name" :: HNil) shouldBe List("name", "name")
  }

  it should "use the generic representation 'Repr' of Person to derive a hListEncoder" in {
    // the compiler actually derived a typeclass instance specially for the Repr of Person
    // how cool is that :)
    CsvEncoder[Person.gen.Repr].encode(Generic[Person].to(Person.Empty)) shouldBe List("name", "42")
  }

  it should "use the generic representation 'Repr' of Address to derive a hListEncoder" in {
    CsvEncoder[Address.gen.Repr].encode(Generic[Address].to(Address.Empty)) shouldBe List("street", "zipcode")
  }
}
