package com.example.reddit.model.methods.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.methods.paging_sources.GetMyUpVoutedPostsPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

class GetMyUpVoutedPostsRepository @Inject constructor(
    @Named("withAuth") private val redditApi: RedditApi
) {
    fun getMySubscribedSubredditsRepository(
        token: String,
        userName: String,
        limit: Int
    ): Flow<PagingData<DataY>> {
        return Pager(PagingConfig(pageSize = 20)) {
            GetMyUpVoutedPostsPagingSource(redditApi, token, userName, limit)
        }.flow
    }
}