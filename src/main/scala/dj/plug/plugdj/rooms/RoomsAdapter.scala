package dj.plug.plugdj.rooms

import android.content.{Context, Intent}
import android.os.Handler
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.BaseAdapter
import dj.plug.plugdj._
import dj.plug.plugdj.player.Broadcasts._
import dj.plug.plugdj.player.PlayerService
import org.json.JSONArray

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class RoomsAdapter(rooms: JSONArray)(implicit context: Context, handler: Handler) extends BaseAdapter {

  override def getCount: Int = rooms.length()

  override def getItem(position: Int): AnyRef = rooms.getJSONObject(position)

  override def getItemId(position: Int): Long = position

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val room = rooms.getJSONObject(position)
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

    viewHolder.population.setText(room.getString("population"))
    viewHolder.guests.setText(room.getString("guests"))

    if (room.getBoolean("favorite")) {
      viewHolder.favorite.setImageResource(R.drawable.ic_star)
    } else {
      viewHolder.favorite.setImageResource(R.drawable.ic_star_border)
    }

    val imageUrl = room.getString("image")
    val bitmapUrl = if (imageUrl.startsWith("//")) {
      "http:" + imageUrl
    } else if (imageUrl.startsWith("/")) {
      "https://cdn.plug.dj" + imageUrl
    } else {
      imageUrl
    }
    getBitmap(bitmapUrl) onComplete {
      case Success(bitmap) => post(() => {
        viewHolder.thumbnail.setImageBitmap(bitmap)
        viewHolder.thumbnail.setMaxHeight((viewHolder.thumbnail.getWidth * .75).toInt)
      })
      case Failure(exception) => Log.e(this, exception.getMessage)
    }

    viewHolder.playing.setText(room.getString("media"))
    viewHolder.roomName.setText(room.getString("name"))
    viewHolder.host.setText(String.format(context.getString(R.string.host), room.getString("host")))
    viewHolder.roomView
  }
}
