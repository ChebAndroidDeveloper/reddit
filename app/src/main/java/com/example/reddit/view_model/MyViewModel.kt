package com.example.reddit.view_model

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.reddit.model.data_classes.data_about_me.InfoAboutMe
import com.example.reddit.model.data_classes.for_get_subreddits.DataX
import com.example.reddit.model.data_classes.my_data.MyData
import com.example.reddit.model.data_classes.test.Children
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.model.data_classes.test.Test
import com.example.reddit.model.data_classes.tests.UserInfo
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.model.methods.repository.ContentSubredditRepository
import com.example.reddit.model.methods.repository.GetMySubscribedSubredditsRepository
import com.example.reddit.model.methods.repository.GetMyUpVoutedPostsRepository
import com.example.reddit.model.methods.repository.PostCommentsRepository
import com.example.reddit.model.methods.repository.RedditAuthRepository
import com.example.reddit.model.methods.repository.SearchedRedditRepository
import com.example.reddit.model.methods.repository.SortedRedditRepository
import com.example.reddit.model.methods.repository.UserCommentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject


@HiltViewModel
class MyViewModel @Inject constructor(
    application: Application,
    private val searchedSrsRepository: SearchedRedditRepository,
    private val sortedSrsRepository: SortedRedditRepository,
    private val redditApiWithAuth: RedditAuthRepository,
    private val srContentRepository: ContentSubredditRepository,
    private val commentsRepository: PostCommentsRepository,
    private val UserCommentsRepository: UserCommentsRepository,
    private val subscribedRepository: GetMySubscribedSubredditsRepository,
    private val myUpVoutedPostsRepository: GetMyUpVoutedPostsRepository,
    private val redditApi: RedditApi
) : AndroidViewModel(application) {
    private var currentQueryValue: String? = null
    private var currentSearchResult: Flow<PagingData<DataX>>? = null
    val token = getTokenFromSharedPreferences()

    fun searchSrsByQuery(query: String): Flow<PagingData<DataX>> {
        val lastResult = currentSearchResult
        if (query == currentQueryValue && lastResult != null) {
            return lastResult
        }
        currentQueryValue = query
        val newResult = searchedSrsRepository.getSubredditsByQuery("Bearer $token", query, 20)
        currentSearchResult = newResult
        return newResult
    }


    fun getSortedSrs(sort: String): Flow<PagingData<DataX>> {
        return sortedSrsRepository.getSortedSubreddits("Bearer $token", sort, 20)
    }

    fun getSrByName(name: String): Flow<PagingData<DataY>> {
        return srContentRepository.getSrByName("Bearer $token", name, 20)
    }

    private val _data = MutableStateFlow<Children?>(null)
    val postInfo: StateFlow<Children?> = _data
    private suspend fun getPost(link: String): Test {
        return redditApi.getLinkInfo("Bearer $token", link)
    }

    suspend fun loadPostData(linkId: String) {
        viewModelScope.launch {
            val result = getPost(linkId)
            _data.value = result.data.children.firstOrNull()
        }
    }


    suspend fun getUserInfo(name: String): UserInfo {
        return redditApi.getUserInfo("Bearer $token", name)
    }


    val error = MutableLiveData<String?>()
    fun addToFriendList(name: String, friendInfo: RedditApi.FriendInfo) {
        viewModelScope.launch {
            try {
                redditApi.addToFriendList("Bearer $token", name, friendInfo)
            } catch (e: HttpException) {
                val errorResponse = e.response()?.errorBody()?.string()
                val jsonObject = errorResponse?.let { JSONObject(it) }
                val explanation = jsonObject?.getString("explanation")
                error.value = explanation

            }
        }
    }

    fun deleteFromFriendList(name: String) {
        viewModelScope.launch {
            try {
                redditApi.removeFromFriendList("Bearer $token", name)
            } catch (e: HttpException) {
                Log.d("TAG", "deleteFromFriendList: ${e.message}")
            }
        }
    }

    private val _commentsList = MutableStateFlow(PagingData.empty<DataY>())
    val commentsList: Flow<PagingData<DataY>> = _commentsList
    fun getCommentsByUser(userName: String) {
        viewModelScope.launch {
            UserCommentsRepository.getComments("Bearer $token", userName, 10)
                .collect { pagingData ->
                    _commentsList.emit(pagingData)
                }
        }
    }

    private val _commentsOfPost = MutableStateFlow(PagingData.empty<DataY>())
    val commentsOfPost: Flow<PagingData<DataY>> = _commentsOfPost

    fun getCommentsOfPost(subredditName: String, postName: String) {
        viewModelScope.launch {
            commentsRepository.getComments("Bearer $token", subredditName, postName)
                .collect { pagingData ->
                    _commentsOfPost.emit(pagingData)
                }

        }
    }


    fun getMySubscribedSubreddits(): Flow<PagingData<DataX>> {
        return subscribedRepository.getMySubscribedSubredditsRepository("Bearer $token", 20)
    }

    fun getMyVoutedComments(): Flow<PagingData<DataY>> {
        return myUpVoutedPostsRepository.getMySubscribedSubredditsRepository(
            "Bearer $token",
            "kostyachu",
            20
        )
    }


    fun subUnSubSr(item: String, subscribe: Boolean) {
        viewModelScope.launch {
            try {
                val action = if (subscribe) "sub" else "unsub"
                redditApiWithAuth.subscribeToSubreddit("Bearer $token", action, item)
                //item.user_is_subscriber = subscribe
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    fun voteUnVote(vote: Int, id: String) {
        viewModelScope.launch {
            try {
                redditApi.voteOnRedditPost("Bearer $token", id, vote)
            } catch (e: Exception) {
                Log.d("TAG", "voteUnVote: ${e.message}")
            }
        }
    }


    suspend fun getInfoAboutMe(): InfoAboutMe {
        return redditApi.getMyInfo("Bearer $token")
    }

    suspend fun getFriendsList(): MyData {
        return redditApi.getMyFriends("Bearer $token")
    }


    private fun getTokenFromSharedPreferences(): String? {
        val sharedPreferences =
            getApplication<Application>().applicationContext.getSharedPreferences(
                "my_preferences",
                Context.MODE_PRIVATE
            )
        return sharedPreferences.getString("access_token", null)
    }


}
