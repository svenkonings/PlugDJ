package dj.plug.plugdj.player

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.os.{Build, Bundle}
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View._
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

  override def onWindowFocusChanged(hasFocus: Boolean): Unit = {
    super.onWindowFocusChanged(hasFocus)
    updateConfiguration(getResources.getConfiguration)
  }

  override def onConfigurationChanged(newConfig: Configuration): Unit = {
    super.onConfigurationChanged(newConfig)
    updateConfiguration(newConfig)
  }

  private def updateConfiguration(configuration: Configuration): Unit = {
    var flags = SYSTEM_UI_FLAG_LAYOUT_STABLE
    if (configuration.orientation == ORIENTATION_LANDSCAPE) flags |= {
      if (Build.VERSION.SDK_INT >= 19) SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
        SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
        SYSTEM_UI_FLAG_HIDE_NAVIGATION |
        SYSTEM_UI_FLAG_FULLSCREEN
      else SYSTEM_UI_FLAG_LOW_PROFILE
    }
    viewHolder.rootView.setSystemUiVisibility(flags)
  }
}
