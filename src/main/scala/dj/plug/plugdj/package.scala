package dj.plug

import java.net.{HttpURLConnection, URL}

import android.graphics.{Bitmap, BitmapFactory}
import android.os.Handler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

package object plugdj {
  def post(function: () => Unit)(implicit handler: Handler): Boolean = handler.postAtFrontOfQueue(new Runnable {
    override def run(): Unit = function()
  })

  def getBitmap(url: String): Future[Bitmap] = Future {
    val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    try {
      BitmapFactory.decodeStream(connection.getInputStream)
    } finally {
      connection.disconnect()
    }
  }
}
