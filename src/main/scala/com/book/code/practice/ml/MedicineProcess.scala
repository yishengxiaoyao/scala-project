package com.book.code.practice.ml

import java.io.BufferedReader
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.types.{ArrayType, DataType, StringType}

import scala.collection.mutable.Set

class MedicineProcess(override val uid: String, private val dict: String)
  extends UnaryTransformer[Seq[String], Seq[String], MedicineProcess] {

  // 词典文件的正则形式
  private val wordsSet = loadDict

  def this(dict: String) = this(Identifiable.randomUID("med"), dict)

  override def copy(extra: ParamMap): MedicineProcess = defaultCopy(extra)

  override protected def createTransformFunc: Seq[String] => Seq[String] = (words: Seq[String]) => {
    // 处理 "单位词", arr 是前面处理过的单元, c 是当前要处理的 word
    words.foldLeft(List[String]())((arr, c) => {
      val newC = wordsSet.contains(c) match {
        case true => List(c, "_MED_")
        case false => List(c)
      }
      arr ++ newC
    })
  }

  override protected def validateInputType(inputType: DataType): Unit = {
    require(inputType.isInstanceOf[ArrayType],
      s"The input column must be ArrayType, but got $inputType.")
  }

  override protected def outputDataType: DataType = new ArrayType(StringType, true)

  // 词典加载
  private def loadDict: Set[String] = {
    val br: BufferedReader = Files.newBufferedReader(Paths.get(dict), StandardCharsets.UTF_8)
    val words = Set[String]()

    var count = 0

    while (br.ready()) {
      words += br.readLine()
      count += 1
    }

    println(s"load med words: $count")

    words
  }
}