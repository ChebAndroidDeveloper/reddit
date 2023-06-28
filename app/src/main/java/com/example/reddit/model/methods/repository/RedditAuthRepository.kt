package com.example.reddit.model.methods.repository

import com.example.reddit.model.methods.RedditApi
import javax.inject.Inject
import javax.inject.Named

class RedditAuthRepository @Inject constructor(
    @Named("withAuth") private val redditApi: RedditApi
) {
    suspend fun subscribeToSubreddit(token: String, action: String, subredditName: String) {
        redditApi.subscribeToSubreddit(token, action, subredditName)
    }

}
