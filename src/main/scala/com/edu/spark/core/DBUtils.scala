package com.edu.spark.core

import scala.util.Random

object DBUtils {

  def getConnection() = {
    new Random().nextInt(10) + ""
  }

  def returnConnection(connection: String) = {

  }

}
