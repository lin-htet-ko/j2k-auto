package com.linhtetko.j2k_auto_android_sample.data.network

import com.linhtetko.j2k_auto_android_sample.data.model.browse.product.ProductResponse
import com.linhtetko.j2k_auto_android_sample.data.model.browse.recipe.RecipeResponse
import com.linhtetko.j2k_auto_android_sample.data.model.feeds.CommentResponse
import com.linhtetko.j2k_auto_android_sample.data.model.feeds.PostResponse
import retrofit2.http.GET

interface ApiService {
    @GET("products")
    suspend fun getProducts(): ProductResponse

    @GET("recipes")
    suspend fun getRecipes(): RecipeResponse

    @GET("posts")
    suspend fun getPosts(): PostResponse

    @GET("comments")
    suspend fun getComments(): CommentResponse
}
