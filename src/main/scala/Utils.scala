package com.jansansad

import query.connection

object Utils {

  val stopWordsSet: Set[String] = Set(
    "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
    "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",

    "the", "is", "to", "this", "it", "not", "and", "or", "of", "on", "in", "for", "with",
    "at", "by", "from", "as", "that", "which", "was", "were", "be", "been", "has", "have",
    "had", "will", "shall", "should", "would", "could", "an", "but", "if", "so", "than",
    "then", "too", "very", "can", "do", "does", "did", "its", "these", "those", "there",
    "their", "they", "them", "he", "she", "him", "her", "you", "your", "yours", "we", "us",
    "our", "ours", "i", "me", "my", "mine", "am", "are", "being", "because", "while", "until",
    "about", "above", "below", "under", "over", "between", "within", "without", "through",
    "during", "before", "after", "again", "once", "some", "any", "each", "every", "other",
    "own", "same", "all", "both", "such", "only", "more", "most", "few", "much", "many",
    "now", "also", "here", "where", "when", "why", "how", "whose", "whom", "what", "which",
    "who", "whichever", "whenever", "wherever", "however", "nor", "either", "neither", "whether",
    "although", "though", "lest", "unless", "cause", "thus", "hence", "therefore", "whereas",
    "further", "furthermore", "moreover", "besides", "indeed", "meanwhile", "nevertheless",
    "nonetheless", "whereby", "wherein", "hereby", "herein", "whereafter", "thereafter",
    "thereupon", "whereupon", "upon", "whence", "whereto", "wheresoever", "whithersoever",
    "whosever", "whatsoever", "whensoever", "howsoever", "whomsoever", "whomso", "whatever",
    "wherever", "whoever", "whomever", "anybody", "somebody", "nobody", "everybody", "everyone",
    "someone", "noone", "anyone", "myself", "yourself", "himself", "herself", "itself", "ourselves",
    "yourselves", "themselves",

    "ain't", "isn't", "wasn't", "weren't", "hasn't", "haven't", "hadn't", "doesn't", "don't",
    "didn't", "won't", "wouldn't", "shouldn't", "couldn't", "can't", "cannot", "mustn't",
    "mightn't", "shan't", "let's", "that's", "who's", "what's", "where's", "when's", "why's",
    "how's", "i'm", "you're", "he's", "she's", "it's", "we're", "they're", "i've", "you've",
    "we've", "they've", "i'd", "you'd", "he'd", "she'd", "we'd", "they'd", "i'll", "you'll",
    "he'll", "she'll", "we'll", "they'll",

    "used", "need", "dare", "eachother", "somethin", "nothing", "anything", "everything",

    ".", ",", "!", "?", ";", ":", "-", "_", "'", "\"", "(", ")", "[", "]", "{", "}", "/", "\\", "|"
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
