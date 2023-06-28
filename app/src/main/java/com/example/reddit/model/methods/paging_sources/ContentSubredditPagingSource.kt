package com.example.reddit.model.methods.paging_sources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.model.methods.RedditApi

class ContentSubredditPagingSource (
    private val redditApi: RedditApi,
    private val token: String,
    private val subredditNames: String,
    private val limit: Int,
) : PagingSource<String, DataY>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, DataY> {
        return try {
            val response = redditApi.getSubredditContent(
                token = token,
                subredditName = subredditNames,
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
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
    override fun getRefreshKey(state: PagingState<String, DataY>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }
}