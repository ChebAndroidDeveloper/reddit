package com.example.reddit.view

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.reddit.R
import com.example.reddit.model.data_classes.for_get_subreddits.DataX
import com.example.reddit.view_model.MyViewModel

@Composable
fun NewsFeed(navController: NavController, viewModel: MyViewModel) {
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("popular") }

    val searchedSubreddits = viewModel.searchSrsByQuery(query).collectAsLazyPagingItems()
    val subredditsBySort = viewModel.getSortedSrs(sort).collectAsLazyPagingItems()

    var checked by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp)) {
        SearchField(query) { query = it }
        SortButtons(checked, query, { checked = it }) { sort = it }
        SubredditList(
            subredditsBySort,
            searchedSubreddits,
            query,
            viewModel,
            navController
        )
    }

}

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.search)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        singleLine = true
    )
}

@Composable
fun SortButtons(
    checked: Boolean,
    query: String,
    onCheckedChange: (Boolean) -> Unit,
    onSortChange: (String) -> Unit
) {
    Row(modifier = Modifier.background(Color.Transparent)) {
        OutlinedIconToggleButton(
            checked = !checked && query.isEmpty(),
            onCheckedChange = {
                onCheckedChange(!it)
                onSortChange("new")
            },
            modifier = Modifier.fillMaxWidth(0.5f),
        ) {
            Text(
                color = if (!checked && query.isEmpty()) Color.White else Color.Black,
                text = stringResource(R.string.new_)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        OutlinedIconToggleButton(
            checked = checked && query.isEmpty(),
            onCheckedChange = {
                onCheckedChange(it)
                onSortChange("popular")
            },
            modifier = Modifier.fillMaxWidth(1f),
        ) {
            Text(
                color = if (checked && query.isEmpty()) Color.White else Color.Black,
                text = stringResource(R.string.popular)
            )
        }
    }
}

@Composable
fun SubredditList(
    subredditsBySort: LazyPagingItems<DataX>,
    searchedSubreddits: LazyPagingItems<DataX>,
    query: String,
    viewModel: MyViewModel,
    navController: NavController

) {
    LazyColumn() {
        val itemsForList = if (query.isNotEmpty()) searchedSubreddits else subredditsBySort
        items(
            count = itemsForList.itemCount,
            key = itemsForList.itemKey(),
            contentType = itemsForList.itemContentType()
        ) { index ->
            val item = itemsForList[index]
            if (item != null) {
                ItemForListReddit(item, viewModel, navController)
            }
        }
        itemsForList.apply {
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
fun ItemForListReddit(item: DataX, viewModel: MyViewModel, navController: NavController) {
    val isExpanded = remember { mutableStateOf(false) }
    val isSubscribed = remember {
        mutableStateOf(item.user_is_subscriber as? Boolean ?: false)
    }
    val itemHeight = animateDpAsState(
        targetValue = if (isExpanded.value) 200.dp else 60.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val context = LocalContext.current
    ElevatedCard(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(itemHeight.value),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Box(modifier = Modifier.clickable { isExpanded.value = !isExpanded.value }) {
            SubredditImage(item)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(Color.Transparent)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SubredditText(item, isExpanded.value)
                }
                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    SubredditSubscribeButton(
                        item, isSubscribed.value, viewModel
                    ) { subscribed ->
                        isSubscribed.value = subscribed
                    }
                }

            }
            if (isExpanded.value) {
                Button(
                    onClick = {
                        val sharedPrefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                        with(sharedPrefs.edit()) {
                            putString("subredditDescription", item.public_description)
                            putString("subredditName", item.display_name)
                            putBoolean("isSubscribed", isSubscribed.value)
                            apply()
                        }
                        navController.navigate("subredditScreen/${item.display_name}")
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).alpha(0.8f)
                ) {
                    Text(text = "go in")
                }
            }
        }
    }
}

@Composable
fun StyledText(text: String, style: TextStyle) {
    Box {
        Text(
            text = text,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White
        )
    }

}

@Composable
fun StyledTextForFullDescription(text: String, style: TextStyle) {
    Box {
        Text(
            text = text,
            style = style,
            color = Color.White
        )
    }
}


@Composable
fun loadImage(imageUrl: String?): Painter? {
    val context = LocalContext.current
    return if (imageUrl != null) {
        rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        )
    } else {
        null
    }
}

@Composable
fun SubredditImage(item: DataX) {
    val painter =
        loadImage(item.banner_img)
            ?: if (item.header_img is String) loadImage(item.header_img) else null
                ?: loadImage(item.icon_img)

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Spacer(modifier = Modifier.height(0.dp))
    }
}

@Composable
fun SubredditText(item: DataX, isExpanded: Boolean) {
    StyledText(
        text = item.display_name,
        style = MaterialTheme.typography.titleLarge
    )
    if (isExpanded) {
        StyledTextForFullDescription(
            text = item.public_description,
            style = MaterialTheme.typography.bodyMedium
        )
    } else {
        StyledText(
            text = item.public_description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SubredditSubscribeButton(
    item: DataX,
    isSubscribed: Boolean,
    viewModel: MyViewModel,
    onSubscribeChanged: (Boolean) -> Unit
) {
    if (isSubscribed) {
        IconButton(onClick = {
            viewModel.subUnSubSr(item.display_name, false)
            onSubscribeChanged(false)
        }) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Subscribed",
                tint = Color.Green
            )
        }
    } else {
        IconButton(onClick = {
            viewModel.subUnSubSr(item.display_name, true)
            onSubscribeChanged(true)
        }) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Not subscribed",
                tint = Color.Red
            )
        }
    }
}



