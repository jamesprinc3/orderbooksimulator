package simulator.logs

import com.github.tototoshi.csv.CSVWriter
import simulator.events.IntPrice

object LogWriter {

  // TODO: make this generic
  def write(log: Log[IntPrice], filePath: String) = {

    val tradeHeader = IntPrice.getCsvHeader
    val tradeData = log.toCsvString
    writeEvents(filePath, tradeHeader, tradeData)
  }

  def writeEvents(filePath: String,
                  header: Seq[String],
                  data: Seq[Seq[String]]): Unit = {

    val writer = CSVWriter.open(filePath)

    writer.writeRow(header)
    writer.writeAll(data)
  }

}
