package com.example.reddit.view

import android.app.Activity
import android.content.Context
import android.webkit.CookieManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.reddit.model.Constants
import com.example.reddit.model.data_classes.data_about_me.InfoAboutMe
import com.example.reddit.model.data_classes.my_data.MyData
import com.example.reddit.model.data_classes.tests.UserInfo
import com.example.reddit.view_model.MyViewModel

@Composable
fun Profile(navController: NavController, viewModel: MyViewModel, context: Context) {
    val infoAboutMe = remember { mutableStateOf<InfoAboutMe?>(null) }
    val friendsList = remember { mutableStateOf<MyData?>(null) }

    val iconUrl = infoAboutMe.value?.icon_img?.let {
        if (it.contains("?")) it.split("?")[0] else it
    }
    LaunchedEffect(Unit) {
        friendsList.value = viewModel.getFriendsList()
        infoAboutMe.value = viewModel.getInfoAboutMe()
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f)
                .align(Alignment.CenterHorizontally)
        ) {
            InfoAboutMe(infoAboutMe, iconUrl, context)
        }
        Box(modifier = Modifier.fillMaxSize(1f)) {
            FriendsList(friendsList, viewModel , navController)

        }
    }
}

@Composable
fun FriendInfo(userName: String, viewModel: MyViewModel , onClick: () -> Unit) {
    val userInfo = remember { mutableStateOf<UserInfo?>(null) }
    LaunchedEffect(userName) {
        userInfo.value = viewModel.getUserInfo(userName)
    }
    val iconUrl = userInfo.value?.data?.icon_img?.let {
        if (it.contains("?")) it.split("?")[0] else it
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Row {
            iconUrl?.let {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current).data(it).build()
                    ), contentDescription = "Author Icon", modifier = Modifier.size(70.dp)
                )
            }
            if (userInfo.value != null) {
                Text(
                    text = userInfo.value!!.data.name,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FriendsList(friends: MutableState<MyData?>, viewModel: MyViewModel , navController: NavController) {
    if (friends.value?.data?.children != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            itemsIndexed(
                friends.value!!.data.children,
                key = { _, friend -> friend.name }) { index, friend ->
                FriendInfo(friend.name, viewModel ,
                    onClick = { navController.navigate("userInfoScreen/${friend.name}") })
            }
        }
    }
}

@Composable
fun LogOut(context: Context) {
    Button(onClick = {
        val sharedPreferences =
            context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("access_token")
        editor.remove("refresh_token")
        editor.apply()

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies(null)

        if (context is Activity) {
            context.finish()
        }

    }) {
        Text(text = "Logout")
    }
}

@Composable
fun InfoAboutMe(userInfo: MutableState<InfoAboutMe?>, iconUrl: String?, context: Context) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        userInfo.value?.name?.let {
            Text(
                text = it,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        iconUrl?.let {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it)
                        .build()
                ),
                contentDescription = "Author Icon",
                modifier = Modifier.size(200.dp)

            )
        }
        LogOut(context)
    }
}







