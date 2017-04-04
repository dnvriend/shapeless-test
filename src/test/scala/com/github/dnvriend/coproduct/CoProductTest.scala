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

package com.github.dnvriend.coproduct

import com.github.dnvriend.TestSpec
import shapeless._
import shapeless.record._
import shapeless.syntax.singleton._

class CoProductTest extends TestSpec {
  // because the generic for Animal is a Coproduct of the subtypes
  // of the sealed trait, we can use the to and from of the generic
  // to map back and forth.
  //
  // When you create a Generic[Animal] or a labelledGeneric[Animal]
  // the resulting product the Generic produces is a Coproduct structure
  // like Inl(Cat.Empty).
  //
  "Generic for sealed trait (Coproduct) Animal" should "create a generic representation of the members of the family" in {
    val gen = Generic[Animal]
    gen.to(Cat.Empty) shouldBe Inl(Cat.Empty)
    gen.to(Dog.Empty) shouldBe Inr(Inl(Dog("Odie", 3)))
    gen.to(Koala.Empty) shouldBe Inr(Inr(Inl(Koala("foo", 10))))
    gen.to(Sloth.Empty) shouldBe Inr(Inr(Inr(Inl(Sloth("bar", 2)))))
  }

  // When you create a Generic for a subtype of the Coproduct
  // like for example the Cat, you get the product representation
  // like Garfield :: 9 :: HNil, so when you want to have a generic
  // representation of the Coproduct family, you should create
  // a generic of the sealed trait
  //
  "Generic for a subtype of the Coproduct family" should "create a generic product representation" in {
    val gen = Generic[Cat]
    gen.to(Cat.Empty) shouldBe "Garfield" :: 9 :: HNil
  }

  it should "also be able to create a Labelled generic representation to be used as record" in {
    val gen = LabelledGeneric[Cat]
    val catRecord = gen.to(Cat.Empty)
    catRecord shouldBe ('name ->> "Garfield") :: ('livesLeft ->> 9) :: HNil
    catRecord('name) shouldBe "Garfield"
    catRecord('livesLeft) shouldBe 9
  }
}
