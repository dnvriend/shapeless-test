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

import shapeless.LabelledGeneric

// encode a sealed family of case classes
sealed trait Animal
case class Cat(name: String, livesLeft: Int) extends Animal
object Cat {
  val Empty = Cat("Garfield", 9)
}
case class Dog(name: String, bonesHidden: Int) extends Animal
object Dog {
  val Empty = Dog("Odie", 3)
}
case class Koala(name: String, leavesEaten: Int) extends Animal
object Koala {
  val Empty = Koala("foo", 10)
}
case class Sloth(name: String, daysToClimbDownFromCurrentTree: Int) extends Animal
object Sloth {
  val Empty = Sloth("bar", 2)
}

/**
 * Encodes a coproduct type, such as a sealed family of case classes.
 *
 * Each constructor from the family gets an encoding in terms of nested Inr and Inl.
 *
 * Which constructor is encoded as Inl() and which as Inr(Inl()) is determined by lexical order
 * of the subclasses. This example illustrates the encoding:
 *
 * scala> val genAnimal = Generic[Animal]
 * genAnimal: shapeless.Generic[Animal]{type Repr = Cat :+: Dog :+: Koala :+: Sloth} = ...
 *
 * scala> def showCoproduct(o: Any) : String = o match {
 *      | case Inl(a) => "Inl(" + showCoproduct(a) + ")"
 *      | case Inr(a) => "Inr(" + showCoproduct(a) + ")"
 *      | case a => a.toString
 *      | }
 * showCoproduct: (o: Any)String
 *
 * scala> showCoproduct(genAnimal.to(garfield))
 * res5: String = Inl(Cat(Garfield,9))
 *
 * scala> showCoproduct(genAnimal.to(odie))
 * res6: String = Inr(Inl(Dog(Odie,3)))
 *
 * scala> showCoproduct(genAnimal.to(koala))
 * res7: String = Inr(Inr(Inl(Koala(foo,10))))
 *
 * scala> showCoproduct(genAnimal.to(sloth))
 * res8: String = Inr(Inr(Inr(Inl(Sloth(bar,2)))))
 *
 * scala>
 * }}}
 */
