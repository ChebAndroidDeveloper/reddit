package com.example.reddit.model.methods.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.reddit.model.data_classes.for_get_subreddits.DataX
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.methods.paging_sources.SearchedSubredditPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Named

class SearchedRedditRepository @Inject constructor(
    @Named("withAuth") private val redditApi: RedditApi
) {
    fun getSubredditsByQuery(token : String, query: String, limit : Int): Flow<PagingData<DataX>> {
        return Pager(PagingConfig(pageSize = 20)) {
            SearchedSubredditPagingSource(redditApi,token, query, limit)
        }.flow
    }
}
