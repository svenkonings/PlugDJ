package dj.plug.plugdj.player

import android.app.{Notification, PendingIntent}
import android.content.{Context, Intent}
import android.graphics.{Bitmap, BitmapFactory}
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
import dj.plug.plugdj.R
import dj.plug.plugdj.player.Broadcasts._

class NotificationManager(val notifyId: Int = 1)(implicit context: Context) {
  private val notificationManager = NotificationManagerCompat.from(context)

  var playWhenReady: Boolean = true
  var title: String = context.getString(R.string.app_name)
  var userName: String = ""
  var bitmap: Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.background)
  var connected: Boolean = true

  def update(): Unit = {
    notificationManager.notify(notifyId, build)
  }

  def cancel(): Unit = {
    notificationManager.cancel(notifyId)
  }

  def build: Notification = {
    val builder = new NotificationCompat.Builder(context)
    builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1))

    val intent = new Intent(context, classOf[PlayerActivity])
    val pendingIntent = PendingIntent.getActivity(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    builder.setContentIntent(pendingIntent)

    def addAction(icon: Int, string: Int, action: String): Unit =
      builder.addAction(icon, context.getString(string), generateAction(action))

    if (playWhenReady) {
      builder.setSmallIcon(R.drawable.ic_play_circle_filled)
      addAction(R.drawable.ic_pause, R.string.pause, ACTION_PAUSE)
    } else {
      builder.setSmallIcon(R.drawable.ic_pause_circle_filled)
      addAction(R.drawable.ic_play_arrow, R.string.play, ACTION_PLAY)
    }
    addAction(R.drawable.ic_stop, R.string.stop, ACTION_STOP)

    builder.setContentTitle(title)
    builder.setContentText(userName)
    builder.setLargeIcon(bitmap)
    if (!connected) {
      builder.setSmallIcon(R.drawable.ic_error)
      builder.setContentText(context.getString(R.string.socket_warning))
    }
    builder.build()
  }

  private def generateAction(action: String): PendingIntent = {
    val intent = new Intent(context, classOf[PlayerService])
    intent.setAction(action)
    PendingIntent.getService(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
  }
}
