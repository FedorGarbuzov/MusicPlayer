package ru.netology.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.netology.musicplayer.db.AppDb
import ru.netology.musicplayer.dto.Album
import ru.netology.musicplayer.repository.PlayerRepository
import ru.netology.musicplayer.repository.PlayerRepositoryImpl
import ru.netology.musicplayer.utils.SingleLiveEvent

class PlayerViewModel(application: Application): AndroidViewModel(application) {

    private val repository: PlayerRepository =
        PlayerRepositoryImpl(AppDb.getInstance(application).trackDao())

    val data = repository.data.asLiveData()

    private val _loadTracksExceptionEvent = SingleLiveEvent<Unit>()
    val loadTracksExceptionEvent: LiveData<Unit>
        get() = _loadTracksExceptionEvent

    suspend fun getAlbum(): Flow<Album> =
        repository.getAlbum()
            .catch { e ->
                e.printStackTrace()
                _loadTracksExceptionEvent.call()
            }

    fun likeById(id: Int) = viewModelScope.launch {
        try {
            repository.likeById(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

