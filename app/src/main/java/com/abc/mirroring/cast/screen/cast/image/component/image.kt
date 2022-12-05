package com.abc.mirroring.cast.screen.cast.image.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import dev.sofi.extentions.SofiComponent
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable


@Composable
@SofiComponent
fun image(
    modifier: Modifier = Modifier,
    image: Streamable,
    callBack: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray)
            .clickable { callBack.invoke() }
    ) {
        /*internal -> get image from uri
        **external -> get image by url
        */
        Image(
            painter = rememberAsyncImagePainter(model = if (image.source() == SourceType.Internal) image.uri() else image.url()),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .aspectRatio(1f),
            contentScale = ContentScale.Fit,
            contentDescription = image.name()
        )
    }
}