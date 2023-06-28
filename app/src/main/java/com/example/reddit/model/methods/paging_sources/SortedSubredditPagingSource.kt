package com.example.reddit.model.methods.paging_sources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.reddit.model.data_classes.for_get_subreddits.DataX

import com.example.reddit.model.methods.RedditApi

class SortedSubredditPagingSource(
    private val redditApi: RedditApi,
    private val token: String,
    private val sort: String,
    private val limit : Int
) : PagingSource<String, DataX>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, DataX> {
        return try {
            val response = redditApi.getSortedSubreddits(
                token = token,
                sort = sort,
                limit = limit,
                after = params.key
            )
            val subreddits = response.data.children.map { it.data }
            LoadResult.Page(
                data = subreddits,
                prevKey = if (response.data.before is String) response.data.before else null,
                nextKey = response.data.after
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
    override fun getRefreshKey(state: PagingState<String, DataX>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }
}