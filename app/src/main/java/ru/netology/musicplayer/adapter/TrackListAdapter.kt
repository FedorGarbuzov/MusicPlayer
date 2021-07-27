package ru.netology.musicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.musicplayer.R
import ru.netology.musicplayer.databinding.TracklistCardBinding
import ru.netology.musicplayer.dto.Track

interface OnInteractionListener {
    fun onPlayMedia(track: Track)
    fun onPause(track: Track)
    fun onLike(track: Track)
}

class TrackListAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Track, TrackListAdapter.TrackViewHolder>(MarkerDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding =
            TracklistCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return TrackViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    class TrackViewHolder(
        private val binding: TracklistCardBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(track: Track) {
            binding.apply {
                trackName.text = track.file
                likeButton.isChecked = track.liked

                likeButton.setOnClickListener {
                    onInteractionListener.onLike(track)
                }

                playButton.setOnClickListener {
                    when (track.playing) {
                        false -> {
                            onInteractionListener.onPlayMedia(track)
                            track.playing = true
                        }

                        else -> {
                            onInteractionListener.onPause(track)
                            track.playing = false
                        }
                    }
                }
                playButton.setIconResource(
                    when (track.playing) {
                        false -> R.drawable.ic_play
                        true -> R.drawable.ic_pause
                    }
                )
            }
        }
    }

    class MarkerDiffCallBack : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
}