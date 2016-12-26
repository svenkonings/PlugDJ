package dj.plug.plugdj.rooms

import android.os.{Bundle, Handler}
import android.support.v7.app.AppCompatActivity
import android.widget.SearchView.OnQueryTextListener
import dj.plug.plugdj.cookies.CookieStorage.loadCookies
import dj.plug.plugdj.{TR, TypedViewHolder}

class RoomActivity extends AppCompatActivity {
  implicit private val context = this
  implicit private val handler = new Handler()

  private var viewHolder: TypedViewHolder.rooms = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    viewHolder = TypedViewHolder.setContentView(this, TR.layout.rooms).asInstanceOf[TypedViewHolder.rooms]
    loadCookies(this)
    val roomsAdapter = new RoomsAdapter()
    viewHolder.roomsView.setAdapter(roomsAdapter)
    viewHolder.searchView.setOnQueryTextListener(new OnQueryTextListener {
      override def onQueryTextSubmit(query: String): Boolean = false

      override def onQueryTextChange(newText: String): Boolean = {
        roomsAdapter.getFilter.filter(newText)
        true
      }
    })
  }
}
