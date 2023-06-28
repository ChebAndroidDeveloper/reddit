package com.example.reddit.model.methods.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.methods.paging_sources.ContentSubredditPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

class ContentSubredditRepository@Inject constructor(
    @Named("withAuth") private val redditApi: RedditApi
) {
    fun getSrByName(token : String, subredditName: String, limit : Int): Flow<PagingData<DataY>> {
        return Pager(PagingConfig(pageSize = 20)) {
            ContentSubredditPagingSource(redditApi,token, subredditName, limit)
        }.flow
    }
}