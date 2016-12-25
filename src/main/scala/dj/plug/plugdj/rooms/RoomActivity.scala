package dj.plug.plugdj.rooms

import android.os.{Bundle, Handler}
import android.support.v7.app.AppCompatActivity
import dj.plug.plugdj.Conversions.stringToJson
import dj.plug.plugdj.cookies.CookieStorage.loadCookies
import dj.plug.plugdj.socket.Socket.rooms
import dj.plug.plugdj.{Log, TR, TypedViewHolder, post}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class RoomActivity extends AppCompatActivity {
  implicit private val context = this
  implicit private val handler = new Handler()

  private var viewHolder: TypedViewHolder.rooms = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    viewHolder = TypedViewHolder.setContentView(this, TR.layout.rooms).asInstanceOf[TypedViewHolder.rooms]
    loadCookies(this)
    // TODO: search rooms
    rooms() onComplete {
      case Success(rooms) => post(() => viewHolder.roomsView.setAdapter(new RoomsAdapter(rooms.getJSONArray("data"))))
      case Failure(exception) => Log.e(this, exception.getMessage)
    }
  }
}
