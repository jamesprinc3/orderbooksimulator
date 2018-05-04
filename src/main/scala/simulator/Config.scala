package simulator

import javafx.scene.paint.Color

import breeze.stats.distributions.{ContinuousDistr, Exponential, LogNormal}
import spray.json._
import DefaultJsonProtocol._

import scala.io.Source

//import java.io.File
//case class Config(foo: Int = -1, out: File = new File("."), xyz: Boolean = false,
//                  libName: String = "", maxCount: Int = -1, verbose: Boolean = false, debug: Boolean = false,
//                  mode: String = "", files: Seq[File] = Seq(), keepalive: Boolean = false,
//                  jars: Seq[File] = Seq(), kwargs: Map[String,String] = Map())

case class Config(simRoot: String = "",
                  distributions: Map[String, ContinuousDistr[Double]] =
                    Map[String, ContinuousDistr[Double]]()) {

  object DistJsonProtocol extends DefaultJsonProtocol {

    implicit object DistListJsonFormat
        extends RootJsonFormat[Map[String, ContinuousDistr[Double]]] {
      def write(dist: Map[String, ContinuousDistr[Double]]) = {
        JsString("Not implemented")
      }

      private def parseDistributionString(
          buyPriceDist: String): ContinuousDistr[Double] = {

        new Exponential(1)
      }

      def read(value: JsValue) = {
        value.asJsObject.getFields("buy_price",
                                   "sell_price",
                                   "buy_cancel_price",
                                   "sell_cancel_price",
                                   "quantity",
                                   "interval") match {
          case Seq(JsString(buyPriceDist),
                   JsString(sellPriceDist),
                   JsString(buyCancelPriceDistStr),
                   JsString(sellCancelPriceDistStr),
                   JsString(quantityDistStr),
                   JsString(intervalDistString)) => {
            println("pattern match successful")

            var distMap = Map[String, ContinuousDistr[Double]]()

            distMap += ("buy_price" -> parseDistributionString(buyPriceDist))

            distMap
          }
          case _ => throw new DeserializationException("Color expected")
        }
      }
    }

    implicit object DistJsonFormat
        extends RootJsonFormat[ContinuousDistr[Double]] {
      def write(dist: ContinuousDistr[Double]) = {
        JsString("Not implemented")
      }

      def read(value: JsValue) = value match {
        case JsString(distString) =>
          val distSplit = distString.split('(')
          distSplit(0) match {
            case "expon" => {
              val coeffsStr = distSplit(1).stripSuffix(")").split(", ")
              val loc = coeffsStr(0).stripPrefix("loc=").toDouble
              val scale = coeffsStr(1).stripPrefix("scale=").toDouble

              println(distSplit(0))
              println("loc: " + loc)
              println("scale: " + scale)

              new Exponential(1/scale)
            }
            case "lognorm" => {
              val coeffsStr = distSplit(1).stripSuffix(")").split(", ")

              val stdDev = coeffsStr(0).stripPrefix("s=").toDouble
              val loc = coeffsStr(1).stripPrefix("loc=").toDouble
              val scale = coeffsStr(2).stripPrefix("scale=").toDouble

              println(distSplit(0))
              println("s: " + stdDev)
              println("loc: " + loc)
              println("scale: " + scale)

              new LogNormal(Math.log(scale), stdDev)
            }
          }


      }
    }

  }

  def parseDistributions(
      distributionsPath: String): Map[String, ContinuousDistr[Double]] = {

    val json = Source.fromFile(distributionsPath).getLines.mkString("")
    val jsonAst = json.parseJson

    println("hello")

    import DistJsonProtocol._
    val distributions = jsonAst.asJsObject
      .fields("distributions")
      .asJsObject
      .fields
      .map(kv => (kv._1, kv._2.convertTo[ContinuousDistr[Double]]))
    println(distributions)
    //.convertTo[Map[String, ContinuousDistr[Double]]]

    distributions
  }
}
