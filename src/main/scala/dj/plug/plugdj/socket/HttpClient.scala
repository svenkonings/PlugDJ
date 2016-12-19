package dj.plug.plugdj.socket

import java.io.{InputStream, OutputStream, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}
import java.util

object HttpClient {
  def headers(address: String): util.Map[String, util.List[String]] = {
    val connection = new URL(address).openConnection().asInstanceOf[HttpURLConnection]
    try {
      connection.getHeaderFields()
    } finally {
      connection.disconnect()
    }
  }

  def get(address: String): String = {
    val connection = new URL(address).openConnection.asInstanceOf[HttpURLConnection]
    connection.setDoInput(true)
    try {
      readConnection(connection)
    } finally {
      connection.disconnect()
    }
  }

  def post(address: String, data: String, contentType: String = "application/json"): String = {
    val connection = new URL(address).openConnection.asInstanceOf[HttpURLConnection]
    connection.setDoOutput(true)
    connection.addRequestProperty("Content-Type", contentType)
    try {
      writeStream(connection.getOutputStream, data)
      readConnection(connection)
    } finally {
      connection.disconnect()
    }
  }

  private def readConnection(connection: HttpURLConnection): String = {
    val errors = connection.getErrorStream()
    if (errors != null) throw RequestException(readStream(errors))
    val responseCode = connection.getResponseCode
    if (invalidResponse(responseCode)) throw RequestException(s"Server returned HTTP response code: $responseCode")
    readStream(connection.getInputStream)
  }

  private def readStream(stream: InputStream): String = scala.io.Source.fromInputStream(stream).mkString

  private def writeStream(stream: OutputStream, data: String): Unit = {
    val writer = new OutputStreamWriter(stream)
    writer.write(data)
    writer.flush()
  }

  private def invalidResponse(responseCode: Int): Boolean = responseCode >= 300

  case class RequestException(msg: String) extends Exception(msg)

}
