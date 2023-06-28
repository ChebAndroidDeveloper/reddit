package com.example.reddit.model.methods.paging_sources

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.data_classes.test.DataY

class UserCommentsPagingSource(
    private val redditApi: RedditApi,
    private val token: String,
    private val userName: String,
    private val limit : Int
) : PagingSource<String, DataY>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, DataY> {
        Log.d("CommentsPagingSource1", "load called with params=$params")
        return try {
            val response = redditApi.getUserComments(
                token = token,
                username = userName,
                limit = limit,
                after = params.key
            )
            Log.d("CommentsPagingSource1", "response=$response")
            val comments = response.data.children.map { it.data }
            //val bodies = response.data.children.map { it.data.body }
            LoadResult.Page(
                data = comments,
                prevKey = if (response.data.before is String) response.data.before else null,
                nextKey = response.data.after
            )
        } catch (e: Exception) {
            Log.d("CommentsPagingSource1", "error=$e")
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