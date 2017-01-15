package dj.plug.plugdj.player

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import dj.plug.plugdj.MainApplication.playerService
import dj.plug.plugdj.rooms.RoomActivity
import dj.plug.plugdj.{TR, TypedViewHolder}

class PlayerActivity extends AppCompatActivity with ServiceListener {
  implicit private val context = this

  private var viewHolder: TypedViewHolder.player = null

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    viewHolder = TypedViewHolder.setContentView(this, TR.layout.player).asInstanceOf[TypedViewHolder.player]
    // TODO: custom controller
    viewHolder.playerView.setUseController(false)
  }

  override def onResume(): Unit = {
    super.onResume()
    val service = playerService
    if (service != null) {
      service.listener = this
      service.setView(viewHolder.playerView)
    } else {
      onBackPressed()
    }
  }

  override def onPause(): Unit = {
    super.onPause()
    val service = playerService
    if (service != null) {
      service.clearView()
      service.listener = null
    }
  }

  override def onBackPressed(): Unit = {
    startActivity(new Intent(this, classOf[RoomActivity]).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK))
    ActivityCompat.finishAfterTransition(this)
  }

  override def stop(): Unit = onBackPressed()
}
