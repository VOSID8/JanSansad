package com.jansansad

import org.apache.spark.sql.DataFrame

object Models {
  case class EntityCount(name: String, count: Int)
  case class EntityDf(name: String, speechDf: DataFrame)
}
