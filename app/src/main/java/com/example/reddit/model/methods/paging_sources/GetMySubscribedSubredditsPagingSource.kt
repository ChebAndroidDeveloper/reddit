package com.example.reddit.model.methods.paging_sources

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.reddit.model.data_classes.for_get_subreddits.DataX
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.model.methods.RedditApi

class GetMySubscribedSubredditsPagingSource(
    private val redditApi: RedditApi,
    private val token: String,
    private val limit: Int,
) : PagingSource<String, DataX>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, DataX> {
        return try {
            val response = redditApi.getSubscribedSubreddits(
                token = token,
                limit= limit,
                after = params.key
            )
            val subreddits = response.data.children.map { it.data }
            LoadResult.Page(
                data = subreddits,
                prevKey = if (response.data.before is String) response.data.before else null,
                nextKey = response.data.after
            )
        } catch (e: Exception) {
            Log.d("PagingSource", "after API call", e)
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