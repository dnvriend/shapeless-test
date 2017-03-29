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

class HListTest extends TestSpec {
  "HList" should "encode different types in a single list" in {

    // several types in a list is known as the 'heterogeneous' list
    val product: String :: Int :: Boolean :: HNil = {
      "Sunday" :: 1 :: false :: HNil
    }

    // the list knows all the types
    product.head shouldBe "Sunday"
    product.drop(1).head shouldBe 1
    product.drop(2).head shouldBe false

    // because the HList must encode a different type per element,
    // the type encoding is not as nice as the normal list:
    val xs: List[Int] = List(1, 2, 3)
    xs should not be 'empty

    // on the other hand, you loose the type
    // of the elements when you put the elements
    // in the normal list
    val ys: List[Any] = List("Sunday", 1, false)
    ys should not be 'empty
  }

  it should "prepend a new element to the list" in {
    val xs: String :: Int :: Boolean :: HNil =
      "Sunday" :: 1 :: false :: HNil

    xs.runtimeLength shouldBe 3

    val ys: Long :: String :: Int :: Boolean :: HNil =
      42L :: xs

    ys.runtimeLength shouldBe 4
  }
}
