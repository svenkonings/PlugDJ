package dj.plug.plugdj.player

import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import dj.plug.plugdj.MainApplication.getPlayerService
import dj.plug.plugdj.player.Broadcasts._
import dj.plug.plugdj.rooms.RoomActivity
import dj.plug.plugdj.{Log, TR, TypedViewHolder}

class PlayerActivity extends AppCompatActivity {
  implicit private val context = this

  private var viewHolder: TypedViewHolder.player = null

  private val receiver = new BroadcastReceiver {
    override def onReceive(context: Context, intent: Intent): Unit = intent.getStringExtra(BROADCAST) match {
      // TODO: display warning and error messages
      case SERVICE_STOPPED => onBackPressed()
      case _ => Log.e(PlayerActivity.this, s"Unknown command: ${intent.getStringExtra(BROADCAST)}")
    }
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    viewHolder = TypedViewHolder.setContentView(this, TR.layout.player).asInstanceOf[TypedViewHolder.player]
    // TODO: custom controller
    viewHolder.playerView.setUseController(false)
  }

  override def onResume(): Unit = {
    super.onResume()
    LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(BROADCAST))
    val service = getPlayerService
    if (service != null) {
      service.setView(viewHolder.playerView)
    } else {
      onBackPressed()
    }
  }

  override def onPause(): Unit = {
    super.onPause()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    val service = getPlayerService
    if (service != null) {
      service.clearView()
    }
  }

  override def onBackPressed(): Unit = {
    startActivity(new Intent(this, classOf[RoomActivity]).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK))
    finishAfterTransition()
  }
}
