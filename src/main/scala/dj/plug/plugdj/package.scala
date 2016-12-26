package dj.plug

import android.content.Context
import android.os.Handler
import com.squareup.picasso.{NetworkPolicy, Picasso, RequestCreator}

package object plugdj {
  def post(function: () => Unit)(implicit handler: Handler): Boolean = handler.postAtFrontOfQueue(new Runnable {
    override def run(): Unit = function()
  })

  def loadImage(path: String)(implicit context: Context): RequestCreator = {
    val picasso = Picasso.`with`(context)
    if (path.isEmpty) {
      picasso.load(R.drawable.background)
    } else {
      picasso.load(path).placeholder(R.drawable.background)
    }.networkPolicy(NetworkPolicy.NO_STORE)
  }
}
