package com.book.code.ml.regression

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.feature.VectorIndexer
import org.apache.spark.ml.regression.{GBTRegressionModel, GBTRegressor}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object GradientBoostedTreeRegressorExample {
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      System.err.println("Usage: <regression_data>")
      System.exit(1)
    }

    val inputData = args(0)

    val conf = new SparkConf().setAppName("GradientBoostedTreeRegressorExample")
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    val data = sqlContext.read.format("libsvm").load(inputData)

    // 自动的识别 categorical features, 对其进行 index， 对于具有不同值 > 4 的特征认为是连续的特征
    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
      .fit(data)

    // 将数据切分为训练和测试集，30% 用于测试
    val Array(trainingData, testData) = data.randomSplit(Array(0.7, 0.3))

    // 训练得到 GBT model
    val gbt = new GBTRegressor()
      .setLabelCol("label")
      .setFeaturesCol("indexedFeatures")
      .setMaxIter(10)

    // 构建 PipeLine
    val pipeline = new Pipeline()
      .setStages(Array(featureIndexer, gbt))

    // 训练模型
    val model = pipeline.fit(trainingData)

    // 进行预测
    val predictions = model.transform(testData)

    // 选择样例行用于展示
    predictions.select("prediction", "label", "features").show(5)

    // 选择 (prediction, true label)，计算 test error
    val evaluator = new RegressionEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")
      .setMetricName("rmse")

    val rmse = evaluator.evaluate(predictions)
    println("Root Mean Squared Error (RMSE) on test data = " + rmse)

    val gbtModel = model.stages(1).asInstanceOf[GBTRegressionModel]
    println("Learned regression GBT model:\n" + gbtModel.toDebugString)

    sc.stop()
  }
}