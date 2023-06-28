package com.example.reddit.model.methods.paging_sources

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.model.data_classes.test.Test
import com.example.reddit.model.methods.RedditApi
import com.google.gson.Gson

class PostCommentsPagingSource(
    private val redditApi: RedditApi,
    private val gson: Gson,
    private val token: String,
    private val subredditName: String,
    private val postName: String,
) : PagingSource<String, DataY>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, DataY> {
        return try {
            val response = redditApi.getCommentsOfPost(
                token = token,
                subreddit = subredditName,
                article = postName,
                after = params.key
            )
            val json = response.string()
            val array = gson.fromJson(json, Array<Test>::class.java)
            val allComments = mutableListOf<DataY>()
            array.forEach { item ->
                val comments = item.data.children.map { it.data }
                allComments.addAll(comments)
            }

            val lastItem = array.last()
            val prevKey = lastItem.data.before as? String
            val nextKey = lastItem.data.after

            LoadResult.Page(
                data = allComments,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            Log.d("777888555", "Error while loading data: $e")
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

