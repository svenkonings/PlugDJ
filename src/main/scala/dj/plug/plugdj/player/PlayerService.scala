package dj.plug.plugdj.player

import android.app.Service
import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.graphics.Bitmap
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.net.{ConnectivityManager, Uri}
import android.os.{IBinder, PowerManager}
import android.support.v4.content.LocalBroadcastManager
import com.neovisionaries.ws.client.WebSocket
import com.squareup.picasso.Picasso.LoadedFrom
import dj.plug.plugdj.MainApplication._
import dj.plug.plugdj.cookies.CookieStorage._
import dj.plug.plugdj.player.Broadcasts._
import dj.plug.plugdj.socket.{Socket, SocketListener}
import dj.plug.plugdj.{Log, R}

import scala.concurrent.Future

class PlayerService extends Service with SocketListener {
  implicit private val context = this

  private var wakeLock: PowerManager#WakeLock = null
  private var notificationManager: NotificationManager = null

  private var player: Player = null
  private var socket: Socket = null

  private var slug: String = null

  private val connectivityReceiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent): Unit = {
      val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
      val activeNetwork = connectivityManager.getActiveNetworkInfo
      val connection = activeNetwork != null && activeNetwork.isConnected
      Log.v(PlayerService.this, s"onReceive: connection=$connection, socket.isConnected=${socket.isConnected}")
      if (!socket.isConnected && connection) connectSocket()
      notificationManager.connected = connection
      // TODO: notify connected
    }
  }

  override def onCreate(): Unit = {
    val powerManager = getSystemService(Context.POWER_SERVICE).asInstanceOf[PowerManager]
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getString(R.string.app_name))
    wakeLock.acquire()

    notificationManager = new NotificationManager()
    startForeground(notificationManager.notifyId, notificationManager.build)

    loadCookies(this)

    player = new Player()
    socket = new Socket(this)
    registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

    setPlayerService(this)
  }


  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    if (intent != null) handleIntent(intent)
    Service.START_NOT_STICKY
  }

  private def handleIntent(intent: Intent): Unit = {
    val action = intent.getAction
    action match {
      case ACTION_PLAY => setPlayWhenReady(true)
      case ACTION_PAUSE => setPlayWhenReady(false)
      case ACTION_STOP => stopSelf()
      case null => // Ignore
      case _ => Log.w(this, s"Unknown action: $action")
    }
    if (intent.hasExtra(SLUG)) {
      slug = intent.getStringExtra(SLUG)
      if (socket.isConnected) socket.join(slug)
    }
    if (intent.getBooleanExtra(START_ACTIVITY, false)) {
      startActivity(new Intent(this, classOf[PlayerActivity])
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK))
    }
  }

  override def onBind(intent: Intent): IBinder = null

  override def onDestroy(): Unit = {
    sendLocalBroadcast(SERVICE_STOPPED)
    setPlayerService(null)

    unregisterReceiver(connectivityReceiver)
    socket.disconnect()
    player.release()

    storeCookies(this)

    notificationManager.cancel()

    wakeLock.release()
  }

  private def sendLocalBroadcast(message: String): Unit = {
    val intent = new Intent(BROADCAST)
    intent.putExtra(BROADCAST, message)
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
  }

  private def connectSocket(): Future[WebSocket] = if (slug != null) socket.connectAndJoin(slug) else socket.connect()

  def getPlayer: Player = player

  def setPlayWhenReady(playWhenReady: Boolean): Unit = {
    player.playWhenReady = playWhenReady
    notificationManager.playWhenReady = playWhenReady
    notificationManager.update()
  }

  override def onAdvance(title: String, author: String): Unit = {
    notificationManager.title = title
    notificationManager.userName = author
    notificationManager.update()
  }

  override def onVideo(uri: Uri, format: Int, startTime: Long): Unit = player.prepare(uri, format, startTime)

  override def onPrepareLoad(drawable: Drawable): Unit = drawable match {
    case bitmapDrawable: BitmapDrawable =>
      notificationManager.bitmap = bitmapDrawable.getBitmap
      notificationManager.update()
    case _ => Log.w(this, s"Invalid drawable received: $drawable")
  }

  override def onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom): Unit = {
    notificationManager.bitmap = bitmap
    notificationManager.update()
  }

  override def onBitmapFailed(errorDrawable: Drawable): Unit = Log.e(this, "Failed to load bitmap")
}
