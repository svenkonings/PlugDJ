package dj.plug.plugdj.socket

import android.graphics.Bitmap
import android.net.Uri

trait SocketListener {
  def onAdvance(title: String, author: String)

  def onVideo(uri: Uri, format: Int, startTime: Long)

  def onBitmap(bitmap: Bitmap)
}
