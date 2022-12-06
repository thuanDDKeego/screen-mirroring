package com.abc.mirroring.cast.screen.cast.image.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sofi.extentions.SofiComponent
import com.abc.mirroring.cast.shared.cast.Command
import com.abc.mirroring.cast.shared.utils.AppDimensions

@Composable
@SofiComponent(private = true, useFor = ["image_player_"])
fun image_player_controller(
    modifier: Modifier = Modifier,
    onControl: (Command) -> Unit
) {
    Row(
        //region
        modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xffffffff))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
        //endregion
    ) {
        IconButton(onClick = { onControl(Command.Previous) }) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.width(AppDimensions.paddingXXL))
        IconButton(onClick = { onControl(Command.Play) }) {
            Image(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.width(AppDimensions.paddingXXL))
        IconButton(onClick = { onControl(Command.Next) }) {
            Icon(
                Icons.Rounded.SkipNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}