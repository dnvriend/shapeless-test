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

trait Converter[A, B] {
  def apply(value: A): B
}

object Converter {
  def pure[A, B](f: A => B): Converter[A, B] = {
    new Converter[A, B] {
      override def apply(value: A): B = f(value)
    }
  }

  implicit class ConverterOps[A](value: A) {
    def to[B](implicit converter: Converter[A, B]): B = {
      converter(value)
    }
  }
}

class ConvertTest extends TestSpec {

}
