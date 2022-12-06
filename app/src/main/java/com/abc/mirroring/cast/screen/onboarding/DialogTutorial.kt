package com.abc.mirroring.cast.screen.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.abc.mirroring.cast.setup.theme.Purple700
import com.abc.mirroring.cast.setup.theme.TypographyM3
import com.abc.mirroring.R

/**
how to call dialog

val showDialog =  remember { mutableStateOf(false) }
if(showDialog.value) {
DialogTutorial{
showDialog.value = it
}
}

setvalue() showDialog.value = true
 */

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DialogTutorial(setShowDialog: (Boolean) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent
        ) {

            val items = ArrayList<OnBoardingData>()

            items.add(
                OnBoardingData(
                    R.drawable.tutorial_cast_1,
                    "TV and Phone must be connected to the same wifi",
                    ""
                )
            )

            items.add(
                OnBoardingData(
                    R.drawable.tutorial_cast_2,
                    "Enable Cast Display on your TV",
                    ""
                )
            )

            items.add(
                OnBoardingData(
                    R.drawable.tutorial_cast_3,
                    "Enable Wireless Display & Choose your TV",
                    ""
                )
            )

            val pagerState = rememberPagerState(
                initialPage = 0
            )

            OnBoardingPager(
                item = items,
                pagerState = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(bottom = 20.dp)
            )

        }
    }
}

@ExperimentalPagerApi
@Composable
fun OnBoardingPager(
    item: List<OnBoardingData>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalPager(state = pagerState, count = item.size) { page ->
                Column(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = item[page].title,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 20.dp, start = 8.dp, end = 8.dp),
                        style = TypographyM3.bodyMedium
                    )

                    Image(
                        painter = painterResource(id = item[page].image),
                        contentDescription = item[page].title,
                        modifier = Modifier
                            .height(250.dp)
                            .fillMaxWidth()
                    )

                }
            }

            PagerIndicator(item.size, pagerState.currentPage)

            // BottomSection(pagerState.currentPage, item.size)
        }


    }
}

@ExperimentalPagerApi
@Composable
fun rememberPagerState(
    @androidx.annotation.IntRange(from = 0) initialPage: Int = 0
): PagerState = rememberSaveable(saver = PagerState.Saver) {
    PagerState(
        currentPage = initialPage
    )
}

@Composable
fun PagerIndicator(size: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(top = 20.dp)
    ) {
        repeat(size) {
            Indicator(isSelected = it == currentPage)
        }
    }
}

@Composable
fun Indicator(isSelected: Boolean) {
    val width = animateDpAsState(targetValue = if (isSelected) 25.dp else 10.dp)

    Box(
        modifier = Modifier
            .padding(1.dp)
            .height(10.dp)
            .width(width.value)
            .clip(CircleShape)
            .background(
                if (isSelected) Purple700 else Color.Gray
            )
    )
}

@Composable
fun BottomSection(currentPage: Int, size: Int) {
    val isEnd = currentPage == size - 1
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = if (isEnd) Arrangement.End else Arrangement.Start
    ) {
        if (isEnd) {
            SkipNextButton("OK", Modifier.padding(end = 20.dp))
        } else {
            SkipNextButton("Skip", Modifier.padding(start = 20.dp))
        }
    }
}

@Composable
fun SkipNextButton(text: String, modifier: Modifier) {
    Text(
        text = text, modifier = modifier, fontSize = 18.sp,
        style = TypographyM3.bodySmall,
        fontWeight = FontWeight.Medium
    )

}