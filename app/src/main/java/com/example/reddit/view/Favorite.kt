package com.example.reddit.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.reddit.model.Constants
import com.example.reddit.model.data_classes.for_get_subreddits.DataX
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.view_model.MyViewModel

@Composable
fun Favorite(navController: NavController, viewModel: MyViewModel) {

    val subscribedSubreddits = viewModel.getMySubscribedSubreddits().collectAsLazyPagingItems()
    val voutedCommentaries = viewModel.getMyVoutedComments().collectAsLazyPagingItems()

    val showScreen = remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                color = if (showScreen.value) Color.Black else Color.White,
                text = "Joined Subreddits",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 20.dp)
            )
            Switch(
                modifier = Modifier
                    .align(Alignment.Center),
                checked = showScreen.value,
                onCheckedChange = { showScreen.value = it }
            )
            Text(
                color = if (!showScreen.value) Color.Black else Color.White,
                text = "Vouted Posts",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 20.dp)
            )

        }
        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (showScreen.value) {
                JoinedSubreddits(subscribedSubreddits, viewModel, navController)
            } else {
                VoutedCommentsList(voutedCommentaries, viewModel, navController)
            }
        }
    }
}


@Composable
fun VoutedCommentsList(
    item: LazyPagingItems<DataY>,
    viewModel: MyViewModel,
    navController: NavController
) {
    LazyColumn {
        items(
            count = item.itemCount,
            key = item.itemKey(),
            contentType = item.itemContentType()
        ) { index ->
            val item = item[index]
            if (item != null) {
                val iconUrl = item.all_awardings.firstOrNull()?.icon_url ?: Constants.PLUG
                Column {
                    ContentBeforePost(
                        item,
                        onClick = { navController.navigate("userInfoScreen/${item.author}") },
                        iconUrl,
                    )

                    ContentOfPost(
                        item,
                        onClick = { navController.navigate("linkInfo/${item.id}/${item.name}") })
                    ButtonsUnderPost(viewModel, item, navController)
                    EndPost()
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

@Composable
fun JoinedSubreddits(
    item: LazyPagingItems<DataX>,
    viewModel: MyViewModel,
    navController: NavController
) {
    LazyColumn() {
        items(
            count = item.itemCount,
            key = item.itemKey(),
            contentType = item.itemContentType()
        ) { index ->
            val item = item[index]
            if (item != null) {
                ItemForListReddit(item, viewModel, navController)
            }
        }
    }
}