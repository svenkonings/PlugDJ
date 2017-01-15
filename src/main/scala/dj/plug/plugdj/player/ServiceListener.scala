package dj.plug.plugdj.player

trait ServiceListener extends EventListener {
  def onConnectionChanged(connection: Boolean): Unit = ()

  def stop(): Unit
}
