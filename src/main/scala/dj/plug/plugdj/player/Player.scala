package dj.plug.plugdj.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.dash.{DashMediaSource, DefaultDashChunkSource}
import com.google.android.exoplayer2.trackselection.{AdaptiveVideoTrackSelection, DefaultTrackSelector}
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.{DefaultBandwidthMeter, DefaultDataSourceFactory}
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.{DefaultLoadControl, ExoPlayer, ExoPlayerFactory, SimpleExoPlayer}
import dj.plug.plugdj.Log
import dj.plug.plugdj.player.Player._
import dj.plug.plugdj.socket.CommunicationHandler.{SOUNDCLOUD, YOUTUBE}

class Player(implicit context: Context) {
  private val handler = new Handler()
  private val bandwidthMeter = new DefaultBandwidthMeter()
  private val userAgent = Util.getUserAgent(context, "PlugDJ")

  private var playWhenReady = true
  private var videoDisabled = false

  private var player: SimpleExoPlayer = null
  private var trackSelector: DefaultTrackSelector = null
  private var currentFormat: Int = 0
  private var prevUri: Uri = null
  private var startTime: Long = 0L

  private var view: SimpleExoPlayerView = null

  def prepare(uri: Uri, format: Int, startTime: Long): Unit = {
    if (uri == prevUri && player != null && player.getPlaybackState != ExoPlayer.STATE_IDLE) return
    Log.v(this, s"prepareYoutube: playWhenReady=$playWhenReady, videoDisabled=$videoDisabled, format=$format")
    release()
    prevUri = uri
    currentFormat = format
    currentFormat match {
      case YOUTUBE => prepareYoutube(uri, startTime)
      case SOUNDCLOUD => prepareSoundCloud(uri, startTime)
      case _ => Log.e(this, s"Unknown format: $format")
    }
  }

  def prepareYoutube(uri: Uri, startTime: Long): Unit = {
    val loadControl = new DefaultLoadControl()
    val videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter)
    trackSelector = new DefaultTrackSelector(handler, videoTrackSelectionFactory)
    player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl)
    applyView()
    applyVideoDisabled()
    applyPlayWhenReady()
    setStartTime(startTime)
    val manifestDataSource = new DefaultDataSourceFactory(context, userAgent)
    val chunkDataSource = new DefaultDataSourceFactory(context, userAgent, bandwidthMeter)
    val chunkSource = new DefaultDashChunkSource.Factory(chunkDataSource)
    val mediaSource = new DashMediaSource(uri, manifestDataSource, chunkSource, null, null)
    player.prepare(mediaSource)
  }

  def prepareSoundCloud(uri: Uri, startTime: Long): Unit = {
    val loadControl = new DefaultLoadControl()
    player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl)
    // TODO: image as view
    applyPlayWhenReady()
    setStartTime(startTime)
    val dataSource = new DefaultDataSourceFactory(context, userAgent, bandwidthMeter)
    val extractors = new DefaultExtractorsFactory()
    val mediaSource = new ExtractorMediaSource(uri, dataSource, extractors, null, null)
    player.prepare(mediaSource)
  }

  def release(): Unit = {
    if (player != null) {
      player.release()
    }
    player = null
    trackSelector = null
    startTime = 0L
  }

  def getPlayWhenReady: Boolean = playWhenReady

  def setPlayWhenReady(value: Boolean): Unit = {
    playWhenReady = value
    applyPlayWhenReady()
  }

  private def applyPlayWhenReady(): Unit = {
    if (player != null) player.setPlayWhenReady(playWhenReady)
    sync()
  }

  def getView: SimpleExoPlayerView = view

  def setView(value: SimpleExoPlayerView): Unit = {
    view = value
    applyView()
  }

  private def applyView(): Unit = if (view != null) view.setPlayer(player)

  private def hasVideo(): Boolean = currentFormat == 1

  def getVideoDisabled: Boolean = videoDisabled

  def setVideoDisabled(value: Boolean): Unit = {
    videoDisabled = value
    applyVideoDisabled()
  }

  private def applyVideoDisabled(): Unit =
    if (trackSelector != null && hasVideo()) trackSelector.setRendererDisabled(VIDEO_TRACK, videoDisabled)


  def setStartTime(value: Long): Unit = {
    startTime = value
    sync()
  }

  private def sync(): Unit = {
    if (player == null || startTime == 0L || !playWhenReady) return
    val currentPosition = System.currentTimeMillis() - startTime
    val difference = player.getCurrentPosition - currentPosition
    if (difference < -MAX_PLAYER_DELAY || difference > MAX_PLAYER_DELAY) player.seekTo(currentPosition + BUFFER_COMPENSATION)
    handler.postDelayed(new Runnable {
      override def run(): Unit = sync()
    }, SYNC_DELAY)
  }
}

object Player {
  private val VIDEO_TRACK = 0
  private val SYNC_DELAY = 1000L
  private val BUFFER_COMPENSATION = 1500L
  private val MAX_PLAYER_DELAY = 3000L
}
