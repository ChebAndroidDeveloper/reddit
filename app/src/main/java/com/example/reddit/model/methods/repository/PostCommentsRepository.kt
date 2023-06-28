package com.example.reddit.model.methods.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.methods.paging_sources.PostCommentsPagingSource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

class PostCommentsRepository @Inject constructor(
    @Named("withAuth") private val redditApi: RedditApi,
    private val gson: Gson
) {
    fun getComments(token: String, subredditName: String, postName: String): Flow<PagingData<DataY>> {
        return Pager(PagingConfig(pageSize = 20)) {
            PostCommentsPagingSource(redditApi, gson, token, subredditName, postName)
        }.flow
    }
}

