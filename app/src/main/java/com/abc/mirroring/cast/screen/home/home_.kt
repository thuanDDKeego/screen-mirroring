package com.abc.mirroring.cast.screen.home

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.R
import com.abc.mirroring.cast.screen.NavGraphs
import com.abc.mirroring.cast.screen.destinations.audio_picker_Destination
import com.abc.mirroring.cast.screen.destinations.screen_mirroring_Destination
import com.abc.mirroring.cast.screen.destinations.setting_Destination
import com.abc.mirroring.cast.screen.destinations.web_cast_Destination
import com.abc.mirroring.cast.screen.destinations.youtube_webview_Destination
import com.abc.mirroring.cast.shared.ui.component._card_small
import com.abc.mirroring.cast.shared.ui.component.card_gradient
import com.abc.mirroring.cast.shared.ui.component.card_reversed
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RootNavGraph(start = true) // sets this as the start destination of the default nav graph
@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun home_(
    @SofiBinding("Do not edit") navigator: DestinationsNavigator,
    @SofiBinding main: GlobalVimel
) {
    val context = LocalContext.current
    //use to handle rating dialog business logic show
    var ratingVisibility by rememberSaveable { mutableStateOf(AppPreferences().countTimeOpenApp!! % 3 == 0 && AppPreferences().isRated == false) }
    val abTestHome: Boolean = AppConfigRemote().ui_home_version?.let { it == 1 } ?: true

    val state by main.state.collectAsState()

    var exitDialogVisibility by remember { mutableStateOf(false) }

    BackHandler {
        exitDialogVisibility = true
    }

    fun adsWrapper(onClick: () -> Unit) {
        if (state.count > 0) {
            main.ads.interstitial?.show(context as Activity) {
                onClick()
            }
        } else {
            main.countInc()
            onClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            /**
             * Top Bar
             */
            topBar = {
                small_top_bar(
                    title = stringResource(id = R.string.app_name),
                    navigator = navigator,
                    navigatorIcon = {
                        IconButton(onClick = { navigator.navigate(setting_Destination()) }) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "setting"
                            )
                        }
                    })
            },
            /**
             * Floating Button
             */
//            floatingActionButton = {
//                FloatingActionButton(
//                    containerColor = Color.White,
//                    onClick = {
//                        navigator.navigate(setting_Destination())
//                    },
//                    content = {
//                        Icon(
//                            Icons.Rounded.QuestionMark,
//                            contentDescription = stringResource(id = R.string.user_guide),
//                        )
//                    },
//                    modifier = Modifier.padding(vertical = 12.dp)
//                )
//            }
        ) { paddingValues ->

            /**
             * <MAIN CONTENT>
             */
            //region @screen home
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                ) {

                    if (abTestHome) {
                        card_reversed(
                            title = stringResource(R.string.screen_mirroring),
                            description = stringResource(id = R.string.screen_mirroring_description),
                            icon = Icons.Rounded.ScreenShare
                        ) {
                            adsWrapper {
                                navigator.navigate(screen_mirroring_Destination())
                            }
                        }

                        card_reversed(
                            title = stringResource(R.string.cast_video),
                            description = stringResource(R.string.cast_video_description),
                            icon = Icons.Rounded.Movie,
                            background = Color(0xffffa069)
                        ) {
                            adsWrapper {
                                navigator.navigate(NavGraphs.video)
                            }
                        }
                    } else {
                        card_gradient(
                            title = stringResource(R.string.screen_mirroring),
                            description = stringResource(id = R.string.screen_mirroring_description),
                            icon = Icons.Rounded.ScreenShare
                        ) {
                            adsWrapper {
                                navigator.navigate(screen_mirroring_Destination())
                            }
                        }

                        card_gradient(
                            title = stringResource(R.string.cast_video),
                            description = stringResource(R.string.cast_video_description),
                            icon = Icons.Rounded.Movie,
                            background = Brush.linearGradient(listOf(Color(0xFF6B4DFF), Color(0xFFAA99FF)))
                        ) {
                            adsWrapper {
                                navigator.navigate(NavGraphs.video)
                            }
                        }
                    }

//            _card_gradient(
//                title = "Điều khiển từ xa cho TV",
//                description = "Điều khiển TV của bạn bằng điện thoại di động có kết nối WiFi",
//                icon = Icons.Rounded.SettingsRemote,
//                background = Brush.verticalGradient(listOf(Color(0xFF6B4DFF), Color(0xFFAA99FF)))
//            ) {
//                navigator.navigate(cast_video_Destination())
//            }

                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Others",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        _card_small(
                            modifier = Modifier.weight(1f),
                            title = "Youtube",
                            icon = Icons.Rounded.SmartDisplay,
                            iconColor = Color(0xfffe6464)
                        ) {
                            adsWrapper {
                                navigator.navigate(youtube_webview_Destination())
                            }
                        }

                        /**
                         * @screen Cast Images
                         */
                        _card_small(
                            modifier = Modifier.weight(1f),
                            title = "Image",
                            icon = Icons.Rounded.Image,
                            iconColor = Color(0xFFFFC800)
                        ) {
                            adsWrapper {
                                navigator.navigate(NavGraphs.image)
                            }
                        }


                        /**
                         * @screen Cast Audio
                         */
                        _card_small(
                            modifier = Modifier.weight(1f),
                            title = "Audio",
                            icon = Icons.Rounded.AudioFile,
                            iconColor = Color(0xFF8040F7)
                        ) {
                            adsWrapper {
                                navigator.navigate(audio_picker_Destination())
                            }
                        }
                    }


                    /**
                     * <MAIN CONTENT/>
                     */
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp)
                    ) {
                        _card_small(
                            modifier = Modifier.weight(1f),
                            title = "Vimeo Video",
                            painter = painterResource(id = R.drawable.vimeo),
                            iconColor = Color(0xff64c9fe),
                            description = "Coming Soon"
                        ) {
                            Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
                        }

                        /**
                         * @screen Cast Images
                         */
                        _card_small(
                            modifier = Modifier.weight(1f),
                            title = "Drive\n",
                            painter = painterResource(id = R.drawable.ic_drive),
                            iconColor = Color(0XFF4CAF51),
                            description = "Coming Soon"
                        ) {
                            Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
                        }


                        /**
                         * @screen Cast Audio
                         */
                        _card_small(
                            modifier = Modifier.weight(1f),
                            title = "Web Browser",
                            icon = Icons.Rounded.TravelExplore,
                            iconColor = Color(0xff64c9fe),
                            description = ""
                        ) {
//                            Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
                            adsWrapper {
                                navigator.navigate(web_cast_Destination())
                            }
                        }
                    }
                }
                //endregion
            }
            if (exitDialogVisibility) {
                main.ads.exitDialog?.render(onHide = { exitDialogVisibility = false }) {
                    adsWrapper {
                        (context as Activity).finish()
                    }
                }
            }
//            if (ratingVisibility) {
//                modal_rate(context = context, hideDialog = { askAgain ->
//                    AppPreferences.isRated = askAgain == true
//                    ratingVisibility = false
//                }) {
//                    AppPreferences.isRated = true
//                    if (it <= 2) {
//                        FeedbackUtils.sendFeedback(context)
//                    } else {
//                        RatingUtils.rateInStore(context as Activity)
//                    }
//                }
//            }

        }
        /**
         * FOOTER CONTENT
         */
        main.ads.native?.small()
    }
}
