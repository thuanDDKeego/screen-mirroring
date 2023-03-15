package com.abc.mirroring.cast.screen.cast.image

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiScreen
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.R
import com.abc.mirroring.cast.screen.cast.image.component.image
import com.abc.mirroring.cast.screen.cast.image.component.image_player_controller
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.setup.graphs.ImageNavGraph
import com.abc.mirroring.cast.shared.cast.SessionPlayer
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.cast.shared.utils.AppDimensions
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking

@ImageNavGraph
@Destination
@Composable
@SofiScreen("Image Player Screen")
fun image_player_(
    @SofiBinding navigator: DestinationsNavigator,
    @SofiBinding main: GlobalVimel,
    @SofiBinding vm: ImageVimel,
    params: ImageParameter,
) {
    val context = LocalContext.current
    /**
     * initial state
     */
    vm.fetch(context, params)

    if (params.current != null) {
        LaunchedEffect(params.current.id()) {
            vm.moveTo(params.current)
        }
    }

    BackHandler {
        FirebaseTracking.log(FirebaseLogEvent.Image_Player_Click_Back)
        navigator.popBackStack()
    }

    /**
     * observe dynamic state
     */
    val state by vm.state.collectAsState()
    val lzState = rememberLazyListState(state.curIdx)

    /**
     * React to the change
     */
    LaunchedEffect(state.counter) {
        if (state.counter >= 5) main.ads.interstitial?.show(context as Activity ){
            vm.reset()
        }
    }

    LaunchedEffect(state.curIdx) {
        lzState.animateScrollToItem(state.curIdx)

        main.caster.cast(vm.current()) {
            //TODO: handle fail
            if (it is SessionPlayer.SessionStatus.Error) {
                Toast.makeText(context, it.error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**
     * @screen Image Picker Screen View
     */
    Column(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            /**
             * @section TOP BAR
             * @see [small_top_bar]
             */
            topBar = {
                small_top_bar(
                    navigatorIcon = {
                        IconButton(onClick = {
                            FirebaseTracking.log(FirebaseLogEvent.Image_Player_Click_Back)
                            navigator.popBackStack()
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigator = navigator,
                    title = stringResource(id = R.string.cast_image)
                )
            }) { padding ->

            Column(
                //region
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                //endregion
            ) {

                /**
                 * @section thumbnail
                 */
                Image(
                    painter = rememberAsyncImagePainter(
                        model = if (params.source == SourceType.Internal) {
                            vm.current().uri()
                        } else {
                            vm.current().url()
                        }  // or ht
                    ),
                    //region
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.40f)
                        .padding(AppDimensions.paddingXXL)
                        .clip(RoundedCornerShape(12.dp))
//                        .aspectRatio(1f),
                    , contentScale = ContentScale.Fit
                    //endregion
                )

                Column(
                    modifier = Modifier
                        .weight(0.60f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    /**
                     * @section Image Slider to pick image
                     */
                    LazyRow(
                        //region
                        modifier = Modifier
                            .height(85.dp)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        state = lzState
                        //endregion
                    ) {
                        itemsIndexed(vm.images) { index, image ->
                            image(image = image) {
                                vm.moveTo(index)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    /**
                     * @section The controller helping to play/pause, next & prev the image slider.
                     */
                    image_player_controller(
                        modifier = Modifier.padding(AppDimensions.paddingXXXL),
                        onControl = vm::onControl
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        main.ads.natives["general"]?.small()
        // main.ads.banner?.render(Modifier.wrapContentSize())
    }
}
