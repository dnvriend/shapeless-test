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
import shapeless.syntax.singleton._
import shapeless.record._
import RecordTest._

object RecordTest {

  case class Car(name: String, color: String)

  object Car {
    final val Empty = Car("Ford", "black")
    val lgen = LabelledGeneric[Car]
  }

}

class RecordTest extends TestSpec {

  it should "Create a record" in {
    val garfieldRecord = {
      ('name ->> "Garfield") ::
        ('color ->> "orange") ::
        HNil
    }

    garfieldRecord('name) shouldBe "Garfield"
    garfieldRecord('color) shouldBe "orange"

    // keys and values extensions added by shapeless.syntax.RecordOps
    garfieldRecord.keys shouldBe 'name :: 'color :: HNil
    garfieldRecord.values shouldBe "Garfield" :: "orange" :: HNil

    val carRecord = LabelledGeneric[Car].to(Car.Empty)
    carRecord('name) shouldBe "Ford"
    carRecord('color) shouldBe "black"

    // the carRecord overwrites the garfield fields
    garfieldRecord.merge(carRecord).values shouldBe "Ford" :: "black" :: HNil

    LabelledGeneric[Car].from(garfieldRecord) shouldBe Car("Garfield", "orange")
    LabelledGeneric[Car].from(carRecord) shouldBe Car("Ford", "black")

    /**
     * def get: Returns the value associated with the singleton typed key k.
     * def fieldAt: Returns the value associated with the singleton typed key k.
     * def updated[V](k: Witness, v: V): Updates or adds to this record a field with key k.
     * def replace[V](k: Witness, v: V): Replaces the value of field k with a value of the same type.
     * def updateWith[W](k: WitnessWith[FSL])(f: k.instance.Out => W): Updates a field having a value with type A by given function.
     * def remove(k : Witness): Remove the field associated with the singleton typed key k, returning both the corresponding value and the updated
     * def +[F](f: F): Updates or adds to this record a field of type F.
     * def -(k: Witness): Remove the field associated with the singleton typed key k, returning the updated record.
     * def merge(m: M): Returns the union of this record and another record.
     * def renameField(oldKey: Witness, newKey: Witness): Rename the field associated with the singleton typed key oldKey. Only available if this record has a field with keyType equal to the singleton type oldKey.T.
     * def keys: Returns the keys of this record as an `HList` of singleton typed values.
     * def values: Returns a `HList` of the values of this record.
     * def fields: Returns a `HList` made of the key-value pairs of this record.
     * def toMap[K, V]: Returns a `Map` whose keys and values are typed as the Lub of the keys and values of this record.
     * def mapValues(f: Poly): Maps a higher rank function across the values of this record.
     * def record: Returns a wrapped version of this record that provides `selectDynamic` access to fields.
     */
  }
}
