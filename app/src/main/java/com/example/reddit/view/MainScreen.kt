package com.example.reddit.view

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.reddit.view_model.MyViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val viewModel : MyViewModel = hiltViewModel()
    val navController = rememberNavController()
    val items = listOf("home", "favorite", "face")
    var selectedItem by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        while (true) {
            val expirationTime = sharedPreferences.getLong("expiration_time", 0L)
            if (System.currentTimeMillis() >= expirationTime) {
                // Token has expired, use refresh_token to obtain a new access_token
                // ...
            }
            delay(60 * 1000) // Check every minute
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.height(35.dp)) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            when (item) {
                                "feed" -> Icon(Icons.Outlined.List, contentDescription = item)
                                "favorite" -> Icon(
                                    Icons.Filled.FavoriteBorder,
                                    contentDescription = item
                                )

                                "face" -> Icon(Icons.Filled.Face, contentDescription = item)
                                else -> Icon(Icons.Filled.List, contentDescription = item)
                            }
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(item)
                        }
                    )
                }
            }
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(navController, startDestination = "home") {
                    composable("home") { NewsFeed(navController = navController, viewModel) }
                    composable("favorite") { Favorite(navController = navController, viewModel) }
                    composable("face") { Profile(navController = navController, viewModel , context) }
                    composable(
                        "subredditScreen/{subredditName}",
                        arguments = listOf(navArgument("subredditName") {
                            type = NavType.StringType
                        })
                    ) { backStackEntry ->
                        val subredditName = backStackEntry.arguments?.getString("subredditName")
                        if (subredditName != null) {
                            SubredditScreen(navController, subredditName)
                        }
                    }
                    composable("linkInfo/{linkId}/{name}",
                        arguments = listOf(navArgument("linkId") { type = NavType.StringType },
                            navArgument("name") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val linkId = backStackEntry.arguments?.getString("linkId")
                        val name = backStackEntry.arguments?.getString("name")
                        if (name != null) {
                            PostInfo(navController, linkId, name)
                        }
                    }
                    composable("userInfoScreen/{userName}",
                        arguments = listOf(navArgument("userName") { type = NavType.StringType },
                        navArgument("userName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName")
                        if (userName != null) {
                            UserInfoScreen(navController, userName)
                        }
                    }
                }
            }
        }
    )
}
