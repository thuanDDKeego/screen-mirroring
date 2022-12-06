package com.abc.mirroring.cast.screen.setting

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.AdUnits
import androidx.compose.material.icons.rounded.AdsClick
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.ScreenShare
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abc.mirroring.BuildConfig
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.R
import com.abc.mirroring.cast.screen.destinations.guideline_Destination
import com.abc.mirroring.cast.shared.utils.FeedbackUtils
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.applovin.sdk.AppLovinSdk
import com.google.android.ads.mediationtestsuite.MediationTestSuite
import dev.sofi.setting.SofiSetting
import dev.sofi.setting._item
import dev.sofi.setting._subtitle
import dev.sofi.setting._switch

@Composable
fun BackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            Icons.Filled.ArrowBack,
            stringResource(R.string.back),
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun setting_(
    navigator: DestinationsNavigator, // private arguments, don't edit,
    @SofiBinding main: GlobalVimel,
    onBackPressed: () -> Unit = { navigator.popBackStack() },
    navigateToDownloadDirectory: () -> Unit = {},
    navigateToTemplate: () -> Unit = {}
) {
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    var ratingVisibility by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.background, scrolledContainerColor = MaterialTheme.colorScheme.background),
                title = {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.settings),
                    )
                },
                navigationIcon = {
                    IconButton(modifier = Modifier.padding(start = 8.dp), onClick = onBackPressed) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            stringResource(id = R.string.back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    main.ads.native?.small()


                    LazyColumn {
                        if (AppConfigRemote().enable_premium == true) {
                            item {
                                SofiSetting._subtitle(text = stringResource(id = R.string.premium))
                            }

                            item {
                                SofiSetting._item(
                                    title = stringResource(id = R.string.unlock_all_features),
                                    icon = Icons.Rounded.Verified,
                                ) {
                                }
                            }

                            item {
                                SofiSetting._item(
                                    title = stringResource(id = R.string.remove_all_ads_permanently),
                                    icon = Icons.Rounded.Verified,
                                ) {
                                }
                            }
                            item {
                                Text(text = stringResource(id = R.string.upgrade_to_premium), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFFFFF), textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(24.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0XFFFFB901))
                                        .clickable {
//                                            navigator.navigate(premium_Destination())
                                        }
                                        .padding(16.dp))

                            }

                            if (AppPreferences().isPremiumSubscribed == true) {
                                item {
                                    SofiSetting._item(
                                        title = stringResource(id = R.string.subscription),
                                        icon = Icons.Rounded.Diamond,
                                    ) {
//                                        navigator.navigate(subscription_Destination())
                                    }
                                }
                            }
                        }

                        item {
                            SofiSetting._subtitle(text = stringResource(id = R.string.general_settings))
                        }
                        item {
                            SofiSetting._item(
                                title = stringResource(R.string.language),
                                icon = Icons.Rounded.Language,
                                description = "English" //TODO: Support i18n soon
                            ) {

                            }
                        }
                        item {
                            SofiSetting._subtitle(text = stringResource(id = R.string.tutorials))
                        }

                        item {
                            SofiSetting._item(
                                title = stringResource(id = R.string.screen_mirroring),
                                description = stringResource(id = R.string.tutorial_screen_mirroring_description),
                                icon = Icons.Rounded.ScreenShare
                            ) {
                                // TODO: Tutorial screen
                            }
                        }
                        item {
                            SofiSetting._item(
                                title = stringResource(id = R.string.tutorial_cast_tv),
                                icon = Icons.Rounded.Cast
                            ) {
                                // TODO: Cast Tutorial screen
                                navigator.navigate(guideline_Destination())
                            }
                        }
                        item {
                            SofiSetting._subtitle(text = stringResource(id = R.string.more))
                        }
                        item {
                            SofiSetting._item(
                                title = stringResource(id = R.string.feedback),
                                icon = Icons.Rounded.RateReview
                            ) {
                                FeedbackUtils.sendFeedback(context)
                            }
                        }
                        item {
                            SofiSetting._item(
                                title = stringResource(id = R.string.share_this_app),
                                icon = Icons.Rounded.Share
                            ) {
                                val intent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Let me recommend you this application:\n https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                                    )
                                    type = "text/plain"
                                }

                                context.startActivity(Intent.createChooser(intent, null))
                            }
                        }

                        item {
                            SofiSetting._item(
                                title = stringResource(id = R.string.rate),
                                icon = Icons.Rounded.RateReview
                            ) {
                                ratingVisibility = true
                            }
                        }

                        item {
                            SofiSetting._item(
                                title = stringResource(id = R.string.privacy_policy),
                                icon = Icons.Rounded.PrivacyTip
                            ) {
                                context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://sofigo.net/policy/")
                                })
                            }
                        }
                        item {
                            SofiSetting._item(
                                title = stringResource(id = R.string.version),
                                description = BuildConfig.VERSION_NAME,
                                icon = Icons.Rounded.NewReleases
                            )
                        }

                        if (BuildConfig.DEBUG) {
                            item {
                                SofiSetting._subtitle(text = "Developer")
                            }
                            item {
                                SofiSetting._item(
                                    title = "Applovin Review",
                                    description = "Click to show debug screen",
                                    icon = Icons.Rounded.AdUnits
                                ) {
                                    AppLovinSdk.getInstance(context).showMediationDebugger()
                                }
                            }
                            item {
                                SofiSetting._item(
                                    title = "Admob Test Suite",
                                    description = "Click to show debug screen",
                                    icon = Icons.Rounded.AdUnits
                                ) {
                                    MediationTestSuite.launch(context)
                                }
                            }
                            item {
                                val enable = main.ads.enable.collectAsState()
                                SofiSetting._switch(
                                    title = "Enable Ads",
                                    icon = Icons.Rounded.AdsClick,
                                    isChecked = enable.value
                                ) {
                                    main.ads.toggle()
                                    if (enable.value) {
                                        main.ads.reload(context as Activity)
                                    }
                                }
                            }
                        }
                    }
                }
//                if (ratingVisibility) {
//                    modal_rate(context = context,
//                        hideDialog =
//                        { askAgain ->
//                            AppPreferences.isRated = askAgain == true
//                            ratingVisibility = false
//                        }) {
//                        AppPreferences.isRated = true
//                        if (it <= 2) {
//                            FeedbackUtils.sendFeedback(context)
//                        } else {
//                            RatingUtils.rateInStore(context as Activity)
//                        }
//                    }
//                }
            }
        })
}

