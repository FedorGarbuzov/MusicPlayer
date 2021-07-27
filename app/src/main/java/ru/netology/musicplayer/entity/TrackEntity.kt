package ru.netology.musicplayer.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.musicplayer.dto.Track

@Entity
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val file: String,
    val liked: Boolean = false,
    var playing: Boolean
) {
    fun toDto() = Track(
        id,
        file,
        liked,
        playing
    )
}

fun Track.toEntity() = TrackEntity(
    id,
    file,
    liked,
    playing
)

fun List<TrackEntity>.toDto(): List<Track> = map(TrackEntity::toDto)
fun List<Track>.toEntity(): List<TrackEntity> = map(Track::toEntity)