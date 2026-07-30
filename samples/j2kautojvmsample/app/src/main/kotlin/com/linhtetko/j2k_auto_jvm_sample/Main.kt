package com.linhtetko.j2k_auto_jvm_sample

import com.linhtetko.j2k_auto_jvm_sample.data.model.feeds.PostResponse
import kotlinx.serialization.json.Json

fun main() {
    val jsonString = """
        {
          "posts": [
            {
              "id": 1,
              "title": "Sample Post",
              "body": "This is a sample post body.",
              "tags": ["sample", "test"],
              "reactions": {
                "likes": 10,
                "dislikes": 2
              },
              "views": 100,
              "userId": 1
            }
          ],
          "total": 1,
          "skip": 0,
          "limit": 10
        }
    """.trimIndent()

    val json = Json { ignoreUnknownKeys = true }
    val response = json.decodeFromString<PostResponse>(jsonString)

    println("Fetched ${response.posts.size} posts.")
    response.posts.forEach { post ->
        println("Post ID: ${post.id}, Title: ${post.title}")
    }
}
