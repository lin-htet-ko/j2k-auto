package com.linhtetko.j2k_auto_android_sample.data.repository

import com.linhtetko.j2k_auto_android_sample.data.network.ApiService

class PostRepository(private val apiService: ApiService) {
    suspend fun getPosts() = apiService.getPosts()
}
