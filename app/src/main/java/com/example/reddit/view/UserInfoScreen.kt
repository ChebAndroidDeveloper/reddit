package com.example.reddit.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.model.data_classes.tests.UserInfo
import com.example.reddit.model.methods.RedditApi
import com.example.reddit.view_model.MyViewModel

@Composable
fun UserInfoScreen(navController: NavController, userName: String) {
    val viewModel: MyViewModel = hiltViewModel()
    val userInfo = remember { mutableStateOf<UserInfo?>(null) }
    LaunchedEffect(userName) {
        userInfo.value = viewModel.getUserInfo(userName)
    }
    val iconUrl = userInfo.value?.data?.icon_img?.let {
        if (it.contains("?")) it.split("?")[0] else it
    }
    val publicDescription = userInfo.value?.data?.subreddit?.public_description ?: "no information"

    LaunchedEffect(userName) {
        viewModel.getCommentsByUser(userName)
    }

    val lazyPagingItems = viewModel.commentsList.collectAsLazyPagingItems()
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .align(Alignment.CenterHorizontally)
        ) {
            UserInfoHeader(
                userInfo = userInfo.value,
                iconUrl = iconUrl,
                publicDescription = publicDescription,

                viewModel,
                userName
            )
        }
        Box(
            modifier = Modifier.weight(1f)
        ) { CommentList(item = lazyPagingItems, viewModel, navController) }
    }
}

@Composable
fun UserInfoHeader(
    userInfo: UserInfo?,
    iconUrl: String?,
    publicDescription: String,
    viewModel: MyViewModel,
    userName: String

) {
    val friendInfo = RedditApi.FriendInfo(userName)

    var state by remember { mutableStateOf(true) }
    var text by remember { mutableStateOf("...") }
    LaunchedEffect(userInfo) {
        text =
            if (userInfo != null && !userInfo.data.is_friend) {
                "Добавить в друзья"
            } else {
                "Уже в друзьях"
            }
        if (text == "Добавить в друзья") state = false else state = true
    }



    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (userInfo != null) {
            Text(
                text = userInfo.data.name, fontSize = 30.sp, fontWeight = FontWeight.Bold
            )
        }
        iconUrl?.let {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current).data(it).build()
                ), contentDescription = "Author Icon", modifier = Modifier.size(200.dp)
            )
        }
        Row {
            Button(onClick = {
                if (!state) {
                    viewModel.addToFriendList(userName, friendInfo)
                    state = true
                } else {
                    viewModel.deleteFromFriendList(userName)
                    state = false
                }
                text = if (text == "Добавить в друзья") "Уже в друзьях" else "Добавить в друзья"

            }) {
                Text(text = text)
            }

        }
        Text(text = publicDescription, fontSize = 20.sp, fontStyle = FontStyle.Italic)

    }
}


@Composable
fun CommentList(
    item: LazyPagingItems<DataY>, viewModel: MyViewModel, navController: NavController
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(
            count = item.itemCount,
            key = item.itemKey(),
            contentType = item.itemContentType(),
        ) { index ->
            val items = item[index]
            if (items != null) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    colors = cardColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        CommentItem(items)
                        ButtonsUnderPost(viewModel, items, navController)
                    }
                }
            }
        }
        item.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    item { ProgressBar() }
                }
                loadState.append is LoadState.Loading -> {
                    item { ProgressBar() }
                }
            }
        }
    }
}



