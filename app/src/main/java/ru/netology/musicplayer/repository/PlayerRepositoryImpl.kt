package ru.netology.musicplayer.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.musicplayer.api.AlbumApi
import ru.netology.musicplayer.dao.TrackDao
import ru.netology.musicplayer.dto.Album
import ru.netology.musicplayer.entity.toDto
import ru.netology.musicplayer.entity.toEntity
import ru.netology.musicplayer.exceptions.ApiException
import ru.netology.musicplayer.exceptions.ServerException
import ru.netology.musicplayer.exceptions.UnknownException
import java.io.IOException

class PlayerRepositoryImpl(
    private val dao: TrackDao
) : PlayerRepository {

    override val data = dao.getAll()
        .map { it.toDto() }
        .flowOn(Dispatchers.Default)

    override suspend fun getAlbum() = flow {
        try {
            val response = AlbumApi.service.getAlbum()

            if (!response.isSuccessful) {
                throw ApiException(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiException(response.code(), response.message())
            dao.insert(body.tracks.toEntity())
            emit(body)
        } catch (e: IOException) {
            throw ServerException
        } catch (e: Exception) {
            throw  UnknownException
        }
    }

    override suspend fun likeById(id: Int) {
        try {
            dao.likeById(id)
        } catch (e: IOException) {
            throw ServerException
        } catch (e: Exception) {
            throw  UnknownException
        }
    }
}