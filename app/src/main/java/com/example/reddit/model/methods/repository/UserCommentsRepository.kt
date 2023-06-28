package com.example.reddit.model.methods.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.methods.paging_sources.UserCommentsPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named
import com.example.reddit.model.data_classes.test.DataY

class UserCommentsRepository@Inject constructor(
    @Named("withAuth") private val redditApi: RedditApi
) {
    fun getComments(token: String, userName: String, limit: Int): Flow<PagingData<DataY>> {
        return Pager(PagingConfig(pageSize = 20)) {
            UserCommentsPagingSource(redditApi, token, userName, limit)
        }.flow
    }
}