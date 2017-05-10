name := "shapeless-test"

organization := "com.github.dnvriend"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.12.2-bin-typelevel-4"

scalaOrganization in ThisBuild := "org.typelevel"

initialize ~= { _ =>
  val ansi = System.getProperty("sbt.log.noformat", "false") != "true"
  if (ansi) System.setProperty("scala.color", "true")
}

scalacOptions += "-Ypartial-unification"

// https://github.com/typelevel/scala/blob/typelevel-readme/notes/typelevel-4.md#faster-compilation-of-inductive-implicits-pull5649-milessabin
scalacOptions += "-Yinduction-heuristics"

// https://github.com/typelevel/scala/blob/typelevel-readme/notes/typelevel-4.md#literal-types-pull5310-milesabin
scalacOptions += "-Yliteral-types"
scalacOptions += "-Xstrict-patmat-analysis"

// https://github.com/typelevel/scala/blob/typelevel-readme/notes/typelevel-4.md#minimal-kind-polymorphism-pull5538-mandubian
scalacOptions += "-Ykind-polymorphism"

// https://github.com/typelevel/scala/blob/typelevel-readme/notes/typelevel-4.md#exhaustivity-of-extractors-guards-and-unsealed-traits-pull5617-sellout
scalacOptions += "-Xstrict-patmat-analysis"
scalacOptions += "-Ydelambdafy:inline"
//scalacOptions in ThisBuild += "-deprecation"

libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3"

val circeVersion = "0.8.0"
libraryDependencies += "io.circe" %% "circe-core" % circeVersion
libraryDependencies += "io.circe" %% "circe-generic" % circeVersion
libraryDependencies += "io.circe" %% "circe-parser" % circeVersion

// testing configuration
fork in Test := true
parallelExecution in Test := false

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable scala code formatting //
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

// Scalariform settings
SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2017", "Dennis Vriend"),
  "conf" -> Apache2_0("2017", "Dennis Vriend", "#")
)

enablePlugins(AutomateHeaderPlugin, SbtScalariform)
