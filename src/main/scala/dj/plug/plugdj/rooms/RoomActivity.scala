package dj.plug.plugdj.rooms

import android.os.{Bundle, Handler}
import android.support.v7.app.AppCompatActivity
import dj.plug.plugdj.cookies.CookieStorage.loadCookies
import dj.plug.plugdj.{TR, TypedViewHolder}

class RoomActivity extends AppCompatActivity {
  implicit private val context = this
  implicit private val handler = new Handler()

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    val viewHolder = TypedViewHolder.setContentView(this, TR.layout.rooms).asInstanceOf[TypedViewHolder.rooms]
    loadCookies(this)
    val roomsAdapter = new RoomsAdapter(viewHolder.searchView, viewHolder.refreshLayout)
    viewHolder.roomsView.setAdapter(roomsAdapter)
  }
}
