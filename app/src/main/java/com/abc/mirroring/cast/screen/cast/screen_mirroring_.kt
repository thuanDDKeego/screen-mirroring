package com.abc.mirroring.cast.screen.cast

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.R
import com.abc.mirroring.cast.screen.onboarding.DialogTutorial
import com.abc.mirroring.cast.shared.ui.component.small_top_bar

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun screen_mirroring_(
    @SofiBinding navigator: DestinationsNavigator
) {
    /**
     * TODO: UI Wifi checking and loadings the devices
     */
    val context = LocalContext.current
    val main: GlobalVimel = GlobalVimel.get()
    // navigate to select devices

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { small_top_bar(navigator = navigator, title = stringResource(id = R.string.screen_mirroring)) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            main.ads.native?.medium()

            Box(
                Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                DialogTutorial {

                }
            }

            Button(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onClick = {
                    main.caster.mirror(context) {
                        Toast.makeText(
                            context,
                            R.string.device_not_supported,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {
                Text(text = stringResource(id = R.string.start))
            }
        }
    }
}