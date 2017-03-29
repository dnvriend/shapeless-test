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

package com.github.dnvriend

import com.github.dnvriend.product.domaina.{ CreditCard => CreditCardA, PaymentType => PaymentTypeA }
import com.github.dnvriend.product.domainb.{ CreditCard => CreditCardB, PaymentType => PaymentTypeB }
import shapeless._

class CoproductTest extends TestSpec {

  // to encode coproducts so 'sealed traits',
  // shapeless has the Coproduct type, which is like the HList but uses
  // :+:, CNil, Inl and Inr as the HList uses :: and HNil

  it should "transform domain-a into domain-b" in {
    val repr = Generic[PaymentTypeA].to(CreditCardA)
    Generic[PaymentTypeA].from(repr)
    //    Generic[PaymentTypeB].from(repr) shouldBe CreditCardB
  }
}
