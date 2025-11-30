package com.example.myapplication.model

data class ExperienceItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val userAvatar: String,
    val userName: String,
    var likeCount: Int,
    var isLiked: Boolean = false,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0
)
