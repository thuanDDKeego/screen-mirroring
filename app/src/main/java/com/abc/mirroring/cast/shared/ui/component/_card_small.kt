package com.abc.mirroring.cast.shared.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@SuppressLint("ComposableNaming")
@Composable
fun _card_small(
    modifier: Modifier = Modifier,
    title: String = "Title ".repeat(2),
    icon: ImageVector = Icons.Rounded.Translate,
    painter: Painter? = null,
    description: String? = null,
    iconColor: Color,
    background: Color = Color.White,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = background,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
//        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .wrapContentSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(iconColor.copy(alpha = 0.15f)),
            ) {
                if (painter == null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp),
                        tint = iconColor
                    )
                } else {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                with(MaterialTheme) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = title,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        style = typography.titleMedium,
                    )
                    if (description != null)
                        Text(
                            text = description,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            style = typography.bodySmall,
                        )
                }
            }
        }
    }
}
