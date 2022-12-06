package com.abc.mirroring.cast.screen.cast.audible.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abc.mirroring.cast.section.Audible
import com.abc.mirroring.cast.section.MediaPicker
import com.abc.mirroring.cast.setup.theme.WhiteAlpha
import com.abc.mirroring.cast.shared.utils.FileUtils

@Composable
fun video(
    modifier: Modifier = Modifier,
    media: Audible,
    callBack: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray)
            .clickable { callBack() }
    ) {

        MediaPicker.getImage(context, media)?.asImageBitmap()?.let {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                bitmap = it,
                contentDescription = media.name
            )
        }

        Row(
            modifier = Modifier
                .background(WhiteAlpha)
                .fillMaxWidth()
                .padding(2.dp)
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Rounded.Videocam,
                contentDescription = null,
                Modifier.size(16.dp),
                tint = Color.White
            )
            Text(
                text = FileUtils.millisToTime(media.duration.toLong()),
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}