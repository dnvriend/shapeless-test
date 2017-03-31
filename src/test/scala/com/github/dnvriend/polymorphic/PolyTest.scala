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

package com.github.dnvriend.polymorphic

import com.github.dnvriend.TestSpec
import shapeless.{ HList, HNil, ::, Poly, Poly1, poly }
import PolyTest._

object PolyTest {
  object MakeBigger extends Poly1 {
    implicit def intCase = at[Int](_ * 100)
    implicit def strCase = at[String](_.toUpperCase)
  }
}

class PolyTest extends TestSpec {
  it should "make 42 bigger" in {
    MakeBigger(42) shouldBe 4200
  }

  it should "uppercase a String" in {
    MakeBigger("foobar") shouldBe "FOOBAR"
  }
}
