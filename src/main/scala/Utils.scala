package com.jansansad

import query.connection

object Utils {

  val stopWordsSet: Set[String] = Set(
    "the", "is", "to", "this", "it", "not", "and", "or", "of", "on", "in", "for", "with", "at", "by", "from", "as", "that"
  )

  val names = populate.getSpeechFolders(5).map(_._1)

  val tablesMap: Map[String, String] = names.map { name =>
    name -> s"${name.replaceAll("\\s+", "").toLowerCase}_word_count"
  }.toMap

   lazy val totalCount: Seq[Double] = Seq(tablesMap.values.toList.map { table =>
    val sql       = s"SELECT SUM(count) FROM $table"
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery(sql)
    if (resultSet.next()) {
      resultSet.getInt(1)
    } else {
      0
    }
  }: _*).map(_.toDouble)

}
