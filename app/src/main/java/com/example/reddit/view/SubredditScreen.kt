package com.example.reddit.view

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.reddit.R
import com.example.reddit.model.Constants
import com.example.reddit.model.data_classes.test.DataY
import com.example.reddit.view_model.MyViewModel


var subredditName: String = ""

@Composable
fun SubredditScreen(navController: NavController, name: String) {
    subredditName = name
    val viewModel: MyViewModel = hiltViewModel()
    val subredditContent = viewModel.getSrByName(name).collectAsLazyPagingItems()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val aboutReddit = sharedPrefs.getString("subredditDescription", null)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (aboutReddit != null) {
            SrInfoOnTop(name, aboutReddit, viewModel)
        }
        LazyColumn {
            items(
                count = subredditContent.itemCount,
                key = subredditContent.itemKey(),
                contentType = subredditContent.itemContentType()
            ) { index ->
                val item = subredditContent[index]
                if (item != null) {
                    val iconUrl = item.all_awardings.firstOrNull()?.icon_url ?: Constants.PLUG
                    val thumbnailUrl = item.thumbnail
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ContentBeforePost(
                            item,
                            onClick = { navController.navigate("userInfoScreen/${item.author}") },
                            iconUrl
                        )
                        Column(
                            modifier = Modifier.clickable { navController.navigate("linkInfo/${item.id}/${name}") },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LoadImage(thumbnailUrl, context)
                            ContentOfPost(
                                item,
                                onClick = { navController.navigate("linkInfo/${item.id}/${name}") })
                        }
                        ButtonsUnderPost(viewModel, item, navController)
                        EndPost()
                    }
                }
            }
            subredditContent.apply {
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
}

@Composable
fun ContentBeforePost(item: DataY, onClick: () -> Unit, iconUrl: String) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(iconUrl)
                    .build()
            ),
            contentDescription = "Author Icon",
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = item.author, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ButtonsUnderPost(viewModel: MyViewModel, item: DataY, navController: NavController) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var score by remember { mutableIntStateOf(item.score) }
        var userVote by remember { mutableIntStateOf(0) }
        val id = item.name
        IconButton(onClick = {
            if (userVote != 1) {
                viewModel.voteUnVote(1, id)
                score += if (userVote == -1) 2 else 1
                userVote = 1
            } else {
                viewModel.voteUnVote(0, id)
                score--
                userVote = 0
            }
        }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Upvote"
            )
        }
        Text(text = score.toString())
        IconButton(onClick = {
            if (userVote != -1) {
                viewModel.voteUnVote(-1, id)
                score -= if (userVote == 1) 2 else 1
                userVote = -1
            } else {
                viewModel.voteUnVote(0, id)
                score++
                userVote = 0
            }
        }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Down-vote"
            )
        }

        IconButton(onClick = { navController.navigate("linkInfo/${item.id}/${subredditName}") })
        {
            Icon(
                painter = painterResource(
                    id = R.drawable.baseline_add_comment_24
                ),
                contentDescription = "Comment",
                modifier = Modifier.alpha(0.5f)
            )
        }
        Text(text = item.num_comments.toString())
        // Кнопка "поделиться"
        val context = LocalContext.current
        val defaultUrl = "https://www.reddit.com"
        val url = item.url ?: defaultUrl
        IconButton(onClick = { share(context, url)

        }) {
            Icon(
                imageVector = Icons.Default.Share, contentDescription = "Share",
                modifier = Modifier.alpha(0.5f)
            )
        }
    }
}


@Composable
fun LoadImage(url: String, context: Context) {
    if (url.isEmpty()) {
        Spacer(modifier = Modifier.size(0.dp))
    } else {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .build()
            ),
            contentDescription = "thumbnail",
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
fun ContentOfPost(item: DataY, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = item.title, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = item.selftext, maxLines = 4, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun EndPost() {
    Spacer(
        modifier = Modifier
            .height(2.dp)
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun SrInfoOnTop(srName: String, srDescription: String, viewModel: MyViewModel) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val isSubscribed = sharedPreferences.getBoolean("isSubscribed", false)
    val name = sharedPreferences.getString("subredditName", "")
    var switchState by remember { mutableStateOf(isSubscribed) }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)){
            Switch(

                checked = switchState,
                onCheckedChange = {
                    switchState = it
                    if (name != null) {
                        viewModel.subUnSubSr(name, switchState)
                    }
                }
            )
            Text(
                text = if (switchState) "joined" else "join"
            )
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = srName,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        IconButton(
            onClick = {
                showDialog.value = true
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(Icons.Default.Info, contentDescription = "Инфо")
        }
    }

    if (showDialog.value) {
        Dialogue(description = srDescription, onDismiss = { showDialog.value = false })
    }
}


@Composable
fun Dialogue(description: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.description)) },
        text = { Text(description) },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text("ОК")
            }
        }
    )
}

fun share(context: Context, url: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Поделиться ссылкой"))
}




