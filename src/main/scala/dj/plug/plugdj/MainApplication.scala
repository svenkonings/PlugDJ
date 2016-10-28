package dj.plug.plugdj

import android.app.{Activity, Application, Service}
import android.content.Context
import dj.plug.plugdj.player.PlayerService

class MainApplication extends Application {
  private var playerService: PlayerService = null

  def getPlayerService: PlayerService = playerService

  def hasPlayerService: Boolean = playerService != null

  def setPlayerService(value: PlayerService): Unit = playerService = value
}

object MainApplication {
  def getMainApplication(implicit context: Context): MainApplication = context match {
    case context: Activity => context.getApplication.asInstanceOf[MainApplication]
    case context: Service => context.getApplication.asInstanceOf[MainApplication]
    case context: Application => context.asInstanceOf[MainApplication]
    case _ => throw new IllegalArgumentException("Not an Activity, Service or Application.")
  }

  def getPlayerService(implicit context: Context): PlayerService = getMainApplication.getPlayerService

  def hasPlayerService(implicit context: Context): Boolean = getMainApplication.hasPlayerService

  def setPlayerService(value: PlayerService)(implicit context: Context): Unit = getMainApplication.setPlayerService(value)
}
