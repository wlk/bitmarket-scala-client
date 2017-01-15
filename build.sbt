import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import scalariform.formatter.preferences._


name := "bitmarket-scala-client"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-unchecked",
  "-deprecation"
)

resolvers ++= Seq("RoundEights" at "http://maven.spikemark.net/roundeights")

libraryDependencies ++= {
  val akkaHttpVersion       = "10.0.1"

  Seq(
    "com.typesafe.akka"    %% "akka-http-core"         % akkaHttpVersion,
    "com.typesafe.akka"    %% "akka-http-spray-json"   % akkaHttpVersion,
    "io.spray"             %% "spray-json"             % "1.3.3",
    "com.roundeights"      %  "hasher_2.11"            % "1.2.0",
    "org.scalatest"        %% "scalatest"              % "3.0.1" % "test"
  )
}


SbtScalariform.scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(SpacesAroundMultiImports, false)
  .setPreference(CompactControlReadability, false)