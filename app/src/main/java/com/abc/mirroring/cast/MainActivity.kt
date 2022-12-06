package com.abc.mirroring.cast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.abc.mirroring.cast.screen.NavGraphs
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.rememberNavHostEngine
import dagger.hilt.android.AndroidEntryPoint
import com.abc.mirroring.cast.screen.cast.audible.AudibleVimel
import com.abc.mirroring.cast.screen.cast.image.ImageVimel
import com.abc.mirroring.cast.screen.cast.youtube.YoutubeVimel
import com.abc.mirroring.cast.screen.destinations.web_cast_Destination
import com.abc.mirroring.cast.setup.theme.CastTvTheme
import com.abc.mirroring.cast.shared.route.MediaRoute
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val MEDIA_ROUTE = "media_route"
    }
    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        var mediaRoute = intent.getStringExtra(MEDIA_ROUTE)
        if(mediaRoute.isNullOrEmpty()) mediaRoute = MediaRoute.Video.route
        val startRoute = when (mediaRoute) {
            MediaRoute.WebCast.route -> web_cast_Destination
            MediaRoute.Image.route -> NavGraphs.image
            MediaRoute.Audio.route -> NavGraphs.audio
            MediaRoute.Youtube.route -> NavGraphs.youtube
            else -> NavGraphs.video
        }
        Timber.i("initialized")

        setContent {
            CastTvTheme {
                // A surface container using the 'background' color from the theme
                CompositionLocalProvider(GlobalState provides GlobalVimel.get()) {

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Attach auto-generated Destination Navigation into activity
                        val engine = rememberNavHostEngine()
                        val navController = engine.rememberNavController()
                        navController.addOnDestinationChangedListener { _, destination, _ ->
                            Timber.v("navigate -> ${destination.route}")
                            destination.route?.let {
                                Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                                    param(FirebaseAnalytics.Param.SCREEN_NAME, it.split("?")[0])
                                    param(FirebaseAnalytics.Param.SCREEN_CLASS, it.split("?")[0])
                                }
                            }
                        }

                        DestinationsNavHost(navGraph = NavGraphs.root, engine = engine, navController = navController, startRoute = startRoute,dependenciesContainerBuilder = dependencies())
                    }
                }
            }
        }
    }

    //region
    @Composable
    fun dependencies(): @Composable DependenciesContainerBuilder<*>.() -> Unit = {

        // 👇 To tie AudiblePlayerVimel to "audible" nested navigation graph,
        // making it available to all screens that belong to it
        dependency(NavGraphs.video) {
            val parent = remember(navBackStackEntry) {
                navController.getBackStackEntry(NavGraphs.video.route)
            }
            hiltViewModel<AudibleVimel>(parent)
        }

        dependency(NavGraphs.audio) {
            val parent = remember(navBackStackEntry) {
                navController.getBackStackEntry(NavGraphs.audio.route)
            }
            hiltViewModel<AudibleVimel>(parent)
        }

        dependency(NavGraphs.image) {
            val parent = remember(navBackStackEntry) {
                navController.getBackStackEntry(NavGraphs.image.route)
            }
            hiltViewModel<ImageVimel>(parent)
        }
        dependency(NavGraphs.youtube) {
            val parent = remember(navBackStackEntry) {
                navController.getBackStackEntry(NavGraphs.youtube.route)
            }
            hiltViewModel<YoutubeVimel>(parent)
        }

        // 👇 To tie ActivityViewModel to the activity, making it available to all destinations
        dependency(GlobalVimel.get())
    }

    //endregion

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}