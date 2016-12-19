package dj.plug.plugdj.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.dash.{DashMediaSource, DefaultDashChunkSource}
import com.google.android.exoplayer2.trackselection.{AdaptiveVideoTrackSelection, DefaultTrackSelector}
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream._
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.{DefaultLoadControl, ExoPlayer, ExoPlayerFactory, SimpleExoPlayer}
import dj.plug.plugdj.Log
import dj.plug.plugdj.player.Player._
import dj.plug.plugdj.socket.CommunicationHandler.{SOUNDCLOUD, YOUTUBE}

class Player(implicit context: Context) {
  private val handler = new Handler()
  private val bandwidthMeter = new DefaultBandwidthMeter()
  private val userAgent = Util.getUserAgent(context, "PlugDJ")

  private var _view: SimpleExoPlayerView = null
  private var _videoDisabled = false
  private var _playWhenReady = true

  private var player: SimpleExoPlayer = null
  private var trackSelector: DefaultTrackSelector = null
  private var currentFormat: Int = 0
  private var prevUri: Uri = null
  private var startTime: Long = 0L

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
    trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory)
    player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl)
    applyView()
    applyVideoDisabled()
    applyPlayWhenReady()
    setStartTime(startTime)
    val manifestDataSource = generateDataSourceFactory(context, userAgent)
    val chunkDataSource = generateDataSourceFactory(context, userAgent, bandwidthMeter)
    val chunkSource = new DefaultDashChunkSource.Factory(chunkDataSource)
    val mediaSource = new DashMediaSource(uri, manifestDataSource, chunkSource, null, null)
    player.prepare(mediaSource)
  }

  def prepareSoundCloud(uri: Uri, startTime: Long): Unit = {
    val loadControl = new DefaultLoadControl()
    trackSelector = new DefaultTrackSelector()
    player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl)
    // TODO: image as view
    applyPlayWhenReady()
    setStartTime(startTime)
    val dataSource = generateDataSourceFactory(context, userAgent, bandwidthMeter)
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

  private def hasVideo(): Boolean = currentFormat == 1

  def view: SimpleExoPlayerView = _view

  def view_=(value: SimpleExoPlayerView): Unit = {
    _view = value
    applyView()
  }

  private def applyView(): Unit = if (view != null) view.setPlayer(player)

  def videoDisabled: Boolean = _videoDisabled

  def videoDisabled_=(value: Boolean): Unit = {
    _videoDisabled = value
    applyVideoDisabled()
  }

  private def applyVideoDisabled(): Unit =
    if (trackSelector != null && hasVideo()) trackSelector.setRendererDisabled(VIDEO_TRACK, videoDisabled)

  def playWhenReady: Boolean = _playWhenReady

  def playWhenReady_=(value: Boolean): Unit = {
    _playWhenReady = value
    applyPlayWhenReady()
  }

  private def applyPlayWhenReady(): Unit = {
    if (player != null) player.setPlayWhenReady(playWhenReady)
    sync()
  }

  private def setStartTime(value: Long): Unit = {
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

  /**
    * Generates a DefaultDataSourceFactory with allowCrossProtocolRedirects set to true.
    */
  private def generateDataSourceFactory(context: Context, userAgent: String, listener: TransferListener[_ >: DataSource] = null): DefaultDataSourceFactory = {
    val httpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent, listener, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS, DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true)
    new DefaultDataSourceFactory(context, listener, httpDataSourceFactory)
  }
}
