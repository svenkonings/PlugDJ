package dj.plug.plugdj.socket

import android.content.Context
import android.net.Uri
import com.squareup.picasso.Target

trait SocketListener extends Context with Target {
  def onAdvance(title: String, author: String)

  def onVideo(uri: Uri, format: Int, startTime: Long)
}
