package dj.plug.plugdj

import android.app.{Activity, Application, Service}
import android.content.Context
import dj.plug.plugdj.player.PlayerService

class MainApplication extends Application {
  private var _playerService: PlayerService = null

  def playerService: PlayerService = _playerService

  def hasPlayerService: Boolean = _playerService != null

  def playerService_=(value: PlayerService): Unit = _playerService = value
}

object MainApplication {
  def mainApplication(implicit context: Context): MainApplication = context match {
    case context: Activity => context.getApplication.asInstanceOf[MainApplication]
    case context: Service => context.getApplication.asInstanceOf[MainApplication]
    case context: Application => context.asInstanceOf[MainApplication]
    case _ => throw new IllegalArgumentException("Not an Activity, Service or Application.")
  }

  def playerService(implicit context: Context): PlayerService = mainApplication.playerService

  def hasPlayerService(implicit context: Context): Boolean = mainApplication.hasPlayerService

  def playerService_=(value: PlayerService)(implicit context: Context): Unit = mainApplication.playerService_=(value)
}
