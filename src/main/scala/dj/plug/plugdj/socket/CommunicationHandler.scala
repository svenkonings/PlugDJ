package dj.plug.plugdj.socket

import java.sql.Timestamp
import java.util.Calendar

import android.os.Handler
import com.neovisionaries.ws.client.{WebSocket, WebSocketAdapter}
import dj.plug.plugdj.Conversions.{stringToJson, stringToUri}
import dj.plug.plugdj.socket.CommunicationHandler._
import dj.plug.plugdj.socket.Events._
import dj.plug.plugdj.socket.Messages._
import dj.plug.plugdj.socket.Socket.state
import dj.plug.plugdj.{Log, loadImage, post}
import org.json.{JSONArray, JSONObject}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class CommunicationHandler(listener: SocketListener) extends WebSocketAdapter {
  implicit private val context = listener
  implicit private val handler = new Handler()

  def applyState(): Unit = state() onComplete {
    case Success(state) =>
      val playback = state.getJSONObject("playback")
      Log.v(this, s"onState: playback=$playback")
      media(playback.getJSONObject("media"), dateToLong(playback.getString("startTime")))
    case Failure(exception) => Log.e(this, exception.getMessage)
  }

  override def onTextMessage(websocket: WebSocket, text: String): Unit = {
    Log.v(this, s"onTextMessage: text=$text")
    if (text == "h") return
    val array = new JSONArray(text)
    for (i <- 0 until array.length()) {
      val json = array.getJSONObject(i)
      val action = json.getString(ACTION)
      val parameters = json.getString(PARAMETER)
      action match {
        case ADVANCE => advance(parameters)
        case _ => // Ignored
        // TODO: support more events
      }
    }
  }

  override def handleCallbackError(websocket: WebSocket, cause: Throwable): Unit = Log.e(this, cause.getMessage)

  private def advance(parameters: JSONObject): Unit =
    media(parameters.getJSONObject("m"), dateToLong(parameters.getString("t")))

  private var currentId: Int = -1

  private def media(media: JSONObject, startTime: Long): Unit = post(() => {
    val id = media.getInt("id")
    if (currentId == id) return
    currentId = id

    val title = media.getString("title")
    val author = media.getString("author")
    Log.v(this, s"onAdvance: title=$title, author=$author")
    listener.onAdvance(title, author)

    val format = media.getInt("format")
    val cid = media.getString("cid")
    format match {
      case YOUTUBE =>
        val uri = s"https://youtube-dash.herokuapp.com/youtube/$cid"
        Log.v(this, s"onVideo: uri=$uri, format=$format, startTime=$startTime")
        listener.onVideo(uri, format, startTime)
      case SOUNDCLOUD =>
        val uri = s"https://api.soundcloud.com/tracks/$cid/stream?client_id=2439302986cfe7971e18a568c879e6c2"
        Log.v(this, s"onVideo: uri=$uri, format=$format, startTime=$startTime")
        listener.onVideo(uri, format, startTime)
      case _ => Log.e(this, s"Unknown format: $format")
    }

    val imageAddress = media.getString("image")
    loadImage(imageAddress).into(listener)
  })
}

object CommunicationHandler {
  val YOUTUBE = 1
  val SOUNDCLOUD = 2

  private def dateToLong(date: String): Long = Timestamp.valueOf(date).getTime + getTimezoneOffset

  private def getTimezoneOffset: Long = {
    val calander = Calendar.getInstance
    calander.get(Calendar.ZONE_OFFSET) + calander.get(Calendar.DST_OFFSET)
  }
}
