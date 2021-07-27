package ru.netology.musicplayer.dto

data class Track(
    val id: Int,
    val file: String,
    val liked: Boolean = false,
    var playing: Boolean
)