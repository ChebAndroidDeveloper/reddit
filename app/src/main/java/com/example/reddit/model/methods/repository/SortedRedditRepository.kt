package com.example.reddit.model.methods.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.reddit.model.data_classes.for_get_subreddits.DataX
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.methods.paging_sources.SortedSubredditPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

class SortedRedditRepository@Inject constructor(
    @Named("withAuth") private val redditApi: RedditApi
) {
    fun getSortedSubreddits(token : String,sort: String, limit: Int): Flow<PagingData<DataX>> {
        return Pager(PagingConfig(pageSize = 20)) {
            SortedSubredditPagingSource(redditApi, token, sort , limit)
        }.flow
    }
}