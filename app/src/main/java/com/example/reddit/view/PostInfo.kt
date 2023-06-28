package com.example.reddit.view

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.reddit.model.Constants
import com.example.reddit.model.data_classes.test.Children
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.view_model.MyViewModel


@Composable
fun PostInfo(navController: NavController, linkId: String?, name: String) {
    val viewModel: MyViewModel = hiltViewModel()
    val context = LocalContext.current
    LaunchedEffect(linkId) {
        linkId?.let {
            viewModel.loadPostData("t3_$it")
        }
    }
    val dataOfPost = viewModel.postInfo.collectAsState().value
    val urlImage = dataOfPost?.data?.thumbnail
    val iconUrl =
        dataOfPost?.data?.all_awardings?.firstOrNull()?.icon_url ?: Constants.PLUG

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .align(Alignment.CenterHorizontally)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                PostBody(
                    dataOfPost,
                    navController,
                    context,
                    urlImage,
                    iconUrl,
                    viewModel
                )
            }
        }
        Box(
            modifier = Modifier.weight(1f)
        ) {
            CommentsOfPost(navController, viewModel, name, linkId)
        }

    }
}

@Composable
fun PostBody(
    dataOfPost: Children?,
    navController: NavController,
    context: Context,
    urlImage: String?,
    iconUrl: String,
    viewModel: MyViewModel
) {
    if (dataOfPost != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ContentBeforePost(
                dataOfPost.data,
                onClick = { navController.navigate("userInfoScreen/${dataOfPost.data.author}") },
                iconUrl
            )
            Text(text = dataOfPost.data.title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = dataOfPost.data.selftext, fontStyle = FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))
            if (urlImage != null) {
                LoadImage(urlImage, context)
            }
            ButtonsUnderPost(viewModel, dataOfPost.data, navController)
        }
    }
}

@Composable
fun CommentsOfPost(
    navController: NavController,
    viewModel: MyViewModel,
    name: String,
    linkId: String?
) {
    val comments = viewModel.commentsOfPost.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        name.let {
            if (linkId != null) {
                viewModel.getCommentsOfPost(it, linkId)
            }
        }
    }

    LazyColumn() {
        items(
            count = comments.itemCount,
            key = comments.itemKey(),
            contentType = comments.itemContentType()
        ) { index ->
            val item = comments[index]
            val iconUrl = item?.all_awardings?.firstOrNull()?.icon_url ?: Constants.PLUG
            if (item != null && item.body != null) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                    {
                        ContentBeforePost(
                            item,
                            onClick = { navController.navigate("userInfoScreen/${item.author}") },
                            iconUrl
                        )
                        Text(text = item.body)
                        ButtonsUnderPost(viewModel, item, navController)
                    }
                }
            }
        }
        comments.apply {
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
@Composable
fun CommentItem(comment: DataY) {
    Column {
        Text(text = comment.body)
    }
}

@Composable
fun ProgressBar() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator()
    }

}



