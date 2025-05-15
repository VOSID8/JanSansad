ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "JanSansad",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.3.0",
      "org.apache.spark" %% "spark-sql" % "3.3.0",
      "org.apache.pdfbox" % "pdfbox" % "2.0.27",
      "org.postgresql" % "postgresql" % "42.5.4",
      "io.github.cibotech" %% "evilplot" % "0.9.0",
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "org.jsoup" % "jsoup" % "1.17.2"
    )
  )
