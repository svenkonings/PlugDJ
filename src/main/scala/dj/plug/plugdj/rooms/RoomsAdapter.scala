package dj.plug.plugdj.rooms

import android.content.{Context, Intent}
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.Filter.FilterResults
import android.widget.SearchView.OnQueryTextListener
import android.widget.{BaseAdapter, Filter, Filterable, SearchView}
import dj.plug.plugdj._
import dj.plug.plugdj.player.Broadcasts._
import dj.plug.plugdj.player.PlayerService
import dj.plug.plugdj.socket.Socket.{getRooms, rooms}
import org.json.JSONArray

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class RoomsAdapter(searchView: SearchView, refreshLayout: SwipeRefreshLayout)(implicit context: Context, handler: Handler) extends BaseAdapter with Filterable {
  private var roomArray = new JSONArray()
  private var query = ""
  private var page = 1
  private val limit = 50

  searchView.setOnQueryTextListener(new OnQueryTextListener {
    override def onQueryTextSubmit(query: String): Boolean = false

    override def onQueryTextChange(newText: String): Boolean = {
      getFilter.filter(newText)
      true
    }
  })

  refreshLayout.setOnRefreshListener(new OnRefreshListener {
    override def onRefresh(): Unit = refresh()
  })

  loadRooms(false)

  def refresh(): Unit = {
    page = 1
    loadRooms(false)
  }

  def nextPage(): Unit = {
    page += 1
    loadRooms(true)
  }

  private def loadRooms(keepOld: Boolean): Unit = {
    startRefresh()
    rooms(query, page, limit) onComplete {
      case Success(newArray) =>
        roomArray = if (keepOld) concat(roomArray, newArray) else newArray
        post(() => stopRefresh(true))
      case Failure(exception) =>
        Log.e(this, exception.getMessage)
        post(() => stopRefresh(false))
    }
  }

  private def startRefresh(): Unit = refreshLayout.setRefreshing(true)

  private def stopRefresh(changed: Boolean): Unit = {
    if (changed) notifyDataSetChanged()
    refreshLayout.setRefreshing(false)
  }

  override def getCount: Int = roomArray.length()

  override def getItem(position: Int): AnyRef = {
    roomArray.getJSONObject(position)
  }

  override def getItemId(position: Int): Long = position

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    if (position == getCount - 1) nextPage()
    val room = roomArray.getJSONObject(position)
    val viewHolder = if (convertView == null) {
      TypedViewHolder.inflate(LayoutInflater.from(context), TR.layout.room, parent, false).asInstanceOf[TypedViewHolder.room]
    } else {
      TypedViewHolder.from(convertView, TR.layout.room).asInstanceOf[TypedViewHolder.room]
    }

    viewHolder.roomView.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = context.startService(
        new Intent(context, classOf[PlayerService])
          .putExtra(SLUG, room.getString("slug"))
          .putExtra(START_ACTIVITY, true))
    })

    viewHolder.population.setText(room.optString("population", "0"))
    viewHolder.guests.setText(room.optString("guests", "0"))

    if (room.optBoolean("favorite", false)) {
      viewHolder.favorite.setImageResource(R.drawable.ic_star)
    } else {
      viewHolder.favorite.setImageResource(R.drawable.ic_star_border)
    }

    val relativeUrl = room.optString("image", "")
    val imageUrl = if (relativeUrl.startsWith("//")) {
      "http:" + relativeUrl
    } else if (relativeUrl.startsWith("/")) {
      "https://cdn.plug.dj" + relativeUrl
    } else {
      relativeUrl
    }
    loadImage(imageUrl).fit().centerCrop().into(viewHolder.thumbnail)

    viewHolder.playing.setText(room.optString("media", ""))
    viewHolder.roomName.setText(room.optString("name", ""))
    viewHolder.host.setText(String.format(context.getString(R.string.host), room.optString("host", "")))
    viewHolder.roomView
  }

  override def getFilter: Filter = new Filter {
    override def publishResults(constraint: CharSequence, results: FilterResults): Unit = results.values match {
      case newArray: JSONArray =>
        roomArray = newArray
        stopRefresh(true)
      case _ =>
        Log.e(this, s"Unknown value: ${results.values}")
        stopRefresh(false)
    }

    override def performFiltering(constraint: CharSequence): FilterResults = {
      post(() => startRefresh())
      query = constraint.toString
      page = 1
      val rooms = getRooms(query, page, limit)
      val filterResults = new FilterResults()
      filterResults.values = rooms
      filterResults.count = rooms.length()
      filterResults
    }
  }
}
