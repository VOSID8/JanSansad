package com.jansansad

import Models._
import com.jansansad.Utils._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import java.io.File
import java.util.Properties
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._

object populate extends App {

  def cleanWord(word: String): String = {
    word
      .replaceAll("[\\u0000-\\u001F]", "")
      .replaceAll("[^\\p{Print}]", "")
      .replaceAll("[.,!?:;…'\"\\[\\]()_*@#]", "")
      .toLowerCase
  }


  def extractTextFromPDF(pdfFile: File): String = {
    val document = PDDocument.load(pdfFile)
    val stripper = new PDFTextStripper()
    val text     = stripper.getText(document)
    document.close()
    cleanWord(text)
  }

  def processPDFs(folderPath: String)(implicit spark: SparkSession): DataFrame = {
    import spark.implicits._

    val pdfFiles = new File(folderPath).listFiles().filter(_.getName.endsWith(".pdf"))

    val textRDD = spark.sparkContext
      .parallelize(pdfFiles)
      .flatMap(file => extractTextFromPDF(file).split("\\s+").map(_.toLowerCase))
      .filter(word => word.nonEmpty && !stopWordsSet.contains(word))

    val wordDF = textRDD.toDF("word")

    val wordCountDF = wordDF.groupBy("word").count().orderBy(desc("count"))

    wordCountDF
  }

  val spark = SparkSession
    .builder()
    .appName("JanSansad")
    .config("spark.driver.memory", "4g")
    .config("spark.executor.memory", "4g")
    .master("local[*]")
    .getOrCreate()

  def getSpeechFolders(limit: Int): List[(String, String)] = {
    val path = Paths.get("LokSabha").toAbsolutePath

    Files.list(path)
      .iterator()
      .asScala
      .filter(Files.isDirectory(_))
      .toList
      .take(limit)
      .map(folder => (folder.getFileName.toString, folder.toAbsolutePath.toString))
  }

  val allDf = getSpeechFolders(5).map { case (name, path) =>
    EntityDf(name, processPDFs(path)(spark))
  }

  val jdbcUrl    = "jdbc:postgresql://localhost:5433/jansansad_db"
  val dbUser     = "postgres"
  val dbPassword = "sansad123"

  val connectionProperties = new Properties()
  connectionProperties.setProperty("user", dbUser)
  connectionProperties.setProperty("password", dbPassword)
  connectionProperties.setProperty("driver", "org.postgresql.Driver")

  allDf.foreach { entity =>
    val tableName = "public." + entity.name.replaceAll("\\s+", "").toLowerCase + "_word_count"
    println(s"Saving DataFrame for ${entity.name} to table: $tableName")

    if (entity.speechDf.isEmpty) {
      println(s"Skipping $tableName: DataFrame is empty")
    } else {
      entity.speechDf.write
        .mode("overwrite")
        .jdbc(jdbcUrl, tableName, connectionProperties)

      println(s"Successfully written $tableName")
    }
  }

  println("All DataFrames successfully written to PostgreSQL!")

  spark.stop()
}