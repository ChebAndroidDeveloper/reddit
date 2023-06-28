package com.example.reddit.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.reddit.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onSkip: () -> Unit, onDontShowAgain: (Boolean) -> Unit) {
    val images = listOf(
        painterResource(id = R.drawable.screen1),
        painterResource(id = R.drawable.screen2),
        painterResource(id = R.drawable.screen3)
    )
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        images.size
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            coroutineScope.launch {
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            Image(
                painter = images[page],
                contentDescription = "Onboarding Image $page",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            var dontShowAgain by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onSkip) {
                    Text("Пропустить")
                }
                Checkbox(
                    checked = dontShowAgain,
                    onCheckedChange = { checked ->
                        dontShowAgain = checked
                        onDontShowAgain(dontShowAgain)
                    }
                )
                Text("Больше не показывать")
            }
        }
    }
}




