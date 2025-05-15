package com.jansansad

import java.sql.{Connection, DriverManager}
import scala.io.StdIn.readLine

import Models._
import Utils.tablesMap
import Utils.totalCount
import PlotApp.plotIt

object query {

  val jdbcUrl    = "jdbc:postgresql://localhost:5433/jansansad_db"
  val dbUser     = "postgres"
  val dbPassword = "sansad123"

  Class.forName("org.postgresql.Driver")
  val connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)

  def main(args: Array[String]): Unit = {

    try {
      def perform(): Unit = {
        val comp = readLine("\n Do you want to search for single word or double (single/double): ").trim.toLowerCase
        if (comp == "single") {
          val word = readLine("\n Enter a word to search: ").trim.toLowerCase
          searchSingleWord(word, connection)
        } else if (comp == "double") {
          val word_1 = readLine("\n Enter first word to compare: ").trim.toLowerCase
          val word_2 = readLine("\n Enter second word to compare: ").trim.toLowerCase
          compareDoubleWords(word_1, word_2, connection)
        } else {
          println("Please enter valid choice")
        }

        val userChoice = readLine("\n Do you want to search again? (yes/no): ").trim.toLowerCase
        if (userChoice == "yes") perform()
        else println("\n Exiting. JanSansad signing off")
      }
      perform()
    } finally {
      connection.close()
    }
  }

  def compareDoubleWords(word1: String, word2: String, connection: Connection): Unit = {
    if (word1.isEmpty || word2.isEmpty) {
      println("Please enter a valid word")
    } else {
      println(s"Searching for the words: '$word1' and $word2 in all tables")
      println(s"For word $word1")
      val result1: List[EntityCount] = tablesMap.map { case(name, table) =>
        val count         = queryWordCount(connection, table, word1)
        val entity = name
        println(s"For $entity (word: $word1): $count")
        EntityCount(entity, count)
      }.toList
      println(s"For word $word2")
      val result2: List[EntityCount] = tablesMap.map { case(name, table) =>
        val count         = queryWordCount(connection, table, word2)
        val entity = name
        println(s"For $entity (word: $word2): $count")
        EntityCount(entity, count)
      }.toList
      val labels: Seq[String] = result1.map(_.name)
      val wordCount: Seq[Seq[Double]]     = Seq(result1.map(_.count), result2.map(_.count))
      val relativeCount: Seq[Seq[Double]] = findRelativeCount(wordCount)
      plotIt(wordCount, relativeCount, Seq(word1, word2), labels)
    }
  }

  def searchSingleWord(word: String, connection: Connection): Unit = {
    if (word.isEmpty) {
      println("Please enter a valid word.")
    } else {
      println(s"Searching for word: '$word' in all tables...\n")
      val results: List[EntityCount] = tablesMap.map { case(name, table) =>
        val count         = queryWordCount(connection, table, word)
        val entity = name
        println(s"For $entity: $count")
        EntityCount(entity, count)
      }.toList
      val labels: Seq[String] = results.map(_.name)
      val wordCount: Seq[Seq[Double]]     = Seq(results.map(_.count))
      val relativeCount: Seq[Seq[Double]] = findRelativeCount(wordCount)
      plotIt(wordCount, relativeCount, Seq(word), labels)
    }
  }

  def findRelativeCount(wordCount: Seq[Seq[Double]]): Seq[Seq[Double]] = {
    wordCount.map(_.zip(totalCount).map { case (a, b) => a / b })
  }

  def queryWordCount(connection: Connection, table: String, word: String): Int = {
    val sql       = s"SELECT SUM(count) FROM $table WHERE word = ?"
    val statement = connection.prepareStatement(sql)

    statement.setString(1, word)

    val resultSet = statement.executeQuery()
    if (resultSet.next()) {
      resultSet.getInt(1)
    } else {
      0
    }
  }
}
