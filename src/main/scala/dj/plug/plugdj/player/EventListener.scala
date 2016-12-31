package dj.plug.plugdj.player

import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.{ExoPlaybackException, ExoPlayer, Timeline}

trait EventListener extends ExoPlayer.EventListener {
  override def onTimelineChanged(timeline: Timeline, manifest: Any): Unit = ()

  override def onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray): Unit = ()

  override def onLoadingChanged(isLoading: Boolean): Unit = ()

  override def onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int): Unit = ()

  override def onPlayerError(error: ExoPlaybackException): Unit = ()

  override def onPositionDiscontinuity(): Unit = ()
}
