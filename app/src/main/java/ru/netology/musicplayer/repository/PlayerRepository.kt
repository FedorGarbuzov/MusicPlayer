package ru.netology.musicplayer.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.musicplayer.dto.Album
import ru.netology.musicplayer.dto.Track

interface PlayerRepository {
    val data: Flow<List<Track>>
    suspend fun getAlbum(): Flow<Album>
    suspend fun likeById(id: Int)
}