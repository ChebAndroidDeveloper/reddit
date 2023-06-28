package com.example.reddit.model.methods


import com.example.reddit.model.data_classes.data_about_me.InfoAboutMe
import com.example.reddit.model.data_classes.for_get_subreddits.RedditResponse
import com.example.reddit.model.data_classes.my_data.MyData
import com.example.reddit.model.data_classes.test.Test
import com.example.reddit.model.data_classes.tests.UserInfo
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RedditApi {
    @GET("/subreddits/search")
    suspend fun searchSubreddits(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): RedditResponse


    @GET("/subreddits/{sort}")
    suspend fun getSortedSubreddits(
        @Header("Authorization") token: String,
        @Path("sort") sort: String,
        @Query("limit") limit: Int,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): RedditResponse

    @FormUrlEncoded
    @POST("/api/subscribe")
    suspend fun subscribeToSubreddit(
        @Header("Authorization") token: String,
        @Field("action") action: String,
        @Field("sr_name") subredditName: String
    )

    @FormUrlEncoded
    @POST("/api/vote")
    suspend fun voteOnRedditPost(
        @Header("Authorization") token: String,
        @Field("id") postId: String,
        @Field("dir") voteDirection: Int
    )

    @GET("/r/{subredditName}")
    suspend fun getSubredditContent(
        @Header("Authorization") token: String,
        @Path("subredditName") subredditName: String,
        @Query("limit") limit: Int,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ): Test

    @GET("/api/info")
    suspend fun getLinkInfo(
        @Header("Authorization") token: String,
        @Query("id") linkName: String
    ) : Test

        @GET("/r/{subreddit}/comments/{article}.json")
    suspend fun getCommentsOfPost(
        @Header("Authorization") token: String,
        @Path("subreddit") subreddit: String,
        @Path("article") article: String,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
    ) : ResponseBody

    @GET("/user/{username}/about")
    suspend fun getUserInfo(
        @Header("Authorization") token: String,
        @Path("username") username: String
        ) : UserInfo

    @PUT("/api/v1/me/friends/{username}")
    suspend fun addToFriendList(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Body friendInfo: FriendInfo
    )
    data class FriendInfo(
        val name: String

    )

    @DELETE("/api/v1/me/friends/{username}")
    suspend fun removeFromFriendList(
        @Header("Authorization") token: String,
        @Path("username") username: String
        ): Response<Unit>?

    @GET ("/user/{username}/comments")
    suspend fun getUserComments(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Query("limit") limit: Int,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
        ) : Test

    @GET ("/subreddits/mine/subscriber")
    suspend fun getSubscribedSubreddits(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
        ) : RedditResponse

    @GET ("/user/{username}/upvoted")
    suspend fun getUserUpvotedPosts(
        @Header("Authorization") token: String,
        @Path("username") username: String,
        @Query("limit") limit: Int,
        @Query("after") after: String? = null,
        @Query("before") before: String? = null
        ) : Test

    @GET ("/api/v1/me")
    suspend fun getMyInfo(
        @Header("Authorization") token: String
        ) : InfoAboutMe

    @GET ("/api/v1/me/friends")
    suspend fun getMyFriends(
        @Header("Authorization") token: String
        ) : MyData






}