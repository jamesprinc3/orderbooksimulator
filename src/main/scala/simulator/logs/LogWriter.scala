package simulator.logs

import com.github.tototoshi.csv.CSVWriter
import simulator.events.Price

object LogWriter {

  def write(log: Log[Price], filePath: String) = {

    val tradeHeader = Price.getCsvHeader
    val tradeData = log.toCsvString
    writeEvents(filePath, tradeHeader, tradeData)
  }

  private def writeEvents(filePath: String,
                          header: Seq[String],
                          data: Seq[Seq[String]]): Unit = {

    val writer = CSVWriter.open(filePath)

    writer.writeRow(header)
    writer.writeAll(data)
  }

}
