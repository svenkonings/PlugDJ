package dj.plug.plugdj.socket

import java.io.IOException
import java.util

import com.neovisionaries.ws.client.{WebSocket, WebSocketFactory}
import dj.plug.plugdj.Conversions.{jsonToString, stringToJson}
import dj.plug.plugdj.Log
import dj.plug.plugdj.socket.HttpClient._
import dj.plug.plugdj.socket.Socket._
import org.json.{JSONArray, JSONObject}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class Socket(playerListener: SocketListener) {
  private val listener: CommunicationHandler = new CommunicationHandler(playerListener)

  private var socket: WebSocket = null

  def join(room: String): Future[String] = {
    Log.v(this, s"Joining room: $room")
    Socket.join(room) andThen {
      case Success(_) => listener.applyState()
    }
  }

  def connect(): Future[WebSocket] = auth() map { auth =>
    socket = new WebSocketFactory().createSocket("wss://godj.plug.dj/socket")
    socket.addHeader("Origin", "https://plug.dj")
    socket.addListener(listener)
    socket.connect()
    Log.v(this, "connected")
    send("auth", auth)
  }

  def connectAndJoin(room: String): Future[WebSocket] = connect() andThen {
    case Success(_) => join(room)
  }

  def isConnected: Boolean = socket != null && socket.isOpen

  def disconnect(): Unit = {
    if (socket != null && socket.isOpen) socket.disconnect()
    socket = null
  }

  def send(action: String, message: String): WebSocket = {
    if (!isConnected) throw new IOException("Not connected.")
    val payload = new JSONObject()
    payload.put("a", action)
    payload.put("p", message)
    payload.put("t", (System.currentTimeMillis / 1000L).toInt)
    Log.v(this, s"send: payload=$payload")
    socket.sendText(payload)
  }
}

object Socket {
  def getAuth(): String = get("https://plug.dj/_/auth/token").getJSONArray("data").getString(0)

  def postJoin(room: String): String = post("https://plug.dj/_/rooms/join", new JSONObject().put("slug", room))

  def getRooms(query: String, page: Int, limit: Int): JSONArray = get(s"https://plug.dj/_/rooms?q=$query&page=$page&limit=$limit").getJSONArray("data")

  def getState(): JSONObject = get("https://plug.dj/_/rooms/state").getJSONArray("data").getJSONObject(0)

  def getHeaders(): util.Map[String, util.List[String]] = headers("https://plug.dj/plug-socket-test")

  private def requestFuture[T](function: () => T): Future[T] = Future {
    function()
  } recover {
    case _: RequestException =>
      getHeaders() // Get the session cookie
      function()
  }

  def auth(): Future[String] = requestFuture(() => getAuth())

  def join(room: String): Future[String] = requestFuture(() => postJoin(room))

  def rooms(query: String, page: Int, limit: Int): Future[JSONArray] = requestFuture(() => getRooms(query, page, limit))

  def state(): Future[JSONObject] = requestFuture(() => getState())
}
