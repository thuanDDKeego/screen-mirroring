package com.abc.mirroring.cast.screen.cast.audible.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abc.mirroring.R
import com.abc.mirroring.cast.section.Audible
import com.abc.mirroring.cast.setup.theme.LightGrayBg
import com.abc.mirroring.cast.shared.utils.FileUtils

@Composable
fun audio(
    modifier: Modifier = Modifier,
    media: Audible,
    callBack: () -> Unit
) {
    val time = FileUtils.millisToTime(media.duration.toLong(), false);
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                callBack.invoke()
            }.padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                modifier = Modifier
                    .background(LightGrayBg, shape = RoundedCornerShape(8.dp))
                    .size(36.dp)
                    .padding(4.dp),
                imageVector = Icons.Outlined.Headphones,
                contentDescription = stringResource(id = R.string.audio),
                tint = Color.White
            )
        }
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = media.name,
                maxLines = 1,
                fontSize = 15.sp
            )
            Text(
                text = "$time • ${FileUtils.getSizeLabel(media.size)}",
                fontSize = 12.sp
            )
        }
    }
}