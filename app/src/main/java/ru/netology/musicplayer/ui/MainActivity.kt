package ru.netology.musicplayer.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.musicplayer.R
import ru.netology.musicplayer.adapter.OnInteractionListener
import ru.netology.musicplayer.adapter.TrackListAdapter
import ru.netology.musicplayer.api.BASE_URL
import ru.netology.musicplayer.databinding.ActivityMainBinding
import ru.netology.musicplayer.dto.Track
import ru.netology.musicplayer.viewmodel.PlayerViewModel

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val viewModel: PlayerViewModel by viewModels()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private val mediaItemTransitionListener: Player.Listener = mediaItemTransitionListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        val adapter = TrackListAdapter(object : OnInteractionListener {
            override fun onPlayMedia(track: Track) {
                initializePlayer(track)
                binding.titlePlayButton.setImageResource(R.drawable.ic_title_pause)
            }

            override fun onPause(track: Track) {
                binding.titlePlayButton.setImageResource(R.drawable.ic_title_play)
                stopPlaying()
                binding.tracklist.adapter?.notifyDataSetChanged()
            }

            override fun onLike(track: Track) {
                viewModel.likeById(track.id)
            }
        })

        lifecycleScope.launchWhenStarted {
            viewModel.getAlbum().collectLatest { album ->
                binding.apply {
                    titleAlbumName.text = album.title
                    titleArtistName.text = album.artist
                    titlePublished.text = album.published
                    titleGenre.text = album.genre
                    titlePlayButton.setOnClickListener {
                        if (player?.isPlaying == false) {
                            initializePlayer(null)
                        } else {
                            stopPlaying()
                            titlePlayButton.setImageResource(R.drawable.ic_title_play)
                            viewModel.data.value?.map { it.playing = false }
                            binding.tracklist.adapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        viewModel.loadTracksExceptionEvent.observe(this, {
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage(R.string.error_loading)
                .setPositiveButton(R.string.dialog_positive_button) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        })

        binding.tracklist.adapter = adapter
        viewModel.data.observe(this, { tracklist ->
            adapter.submitList(tracklist)
        })
    }

    public override fun onResume() {
        super.onResume()
        initializePlayer(null)
    }

    public override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    public override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer(track: Track?) {
        binding.playerView.visibility = View.VISIBLE
        player = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.playerView.player = exoPlayer
                val mediaItems = mutableListOf<MediaItem>()
                viewModel.data.value?.forEach { track ->
                    mediaItems.addAll(
                        listOf(MediaItem.fromUri(BASE_URL + track.file))
                    )
                }
                if (track != null) {
                    val mediaItem = MediaItem.fromUri(BASE_URL + track.file)
                    val currentTrack = mediaItems.indexOf(mediaItem)
                    currentWindow = currentTrack
                }
                stopPlaying()
                exoPlayer.addMediaItems(mediaItems)
                exoPlayer.addListener(mediaItemTransitionListener)
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
                exoPlayer.repeatMode
            }
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(mediaItemTransitionListener)
            release()
        }
        player = null
    }

    private fun stopPlaying() {
        player?.run {
            release()
        }
    }

    private fun mediaItemTransitionListener() = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            lifecycleScope.launch {
                when (reason) {
                    ExoPlayer.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
                        refreshUI()
                    }
                    Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
                        delay(10)
                        if (player?.isPlaying == true) refreshUI()
                    }
                    else -> null
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            when (isPlaying) {
                false -> {
                    viewModel.data.value?.map { it.playing = false }
                    binding.titlePlayButton.setImageResource(R.drawable.ic_title_play)
                    binding.tracklist.adapter?.notifyDataSetChanged()
                }
                true -> {
                    binding.titlePlayButton.setImageResource(R.drawable.ic_title_pause)
                    refreshUI()
                }
            }
        }
    }

    private fun refreshUI() {
        viewModel.data.value?.let { tracklist ->
            tracklist.map { it.playing = false }
            player?.let { currentWindow = player!!.currentWindowIndex }
            val playingTrack = tracklist.find { it.id == player?.nextWindowIndex }
            playingTrack?.playing = true
            binding.tracklist.adapter?.notifyDataSetChanged()
        }
    }
}