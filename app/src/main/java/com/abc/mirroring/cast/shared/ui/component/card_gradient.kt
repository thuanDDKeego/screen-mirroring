package com.abc.mirroring.cast.shared.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@SuppressLint("ComposableNaming")
@Composable
fun card_gradient(
    modifier: Modifier = Modifier,
    title: String = "Title ".repeat(2),
    description: String? = "Description text ".repeat(3),
    icon: ImageVector? = Icons.Outlined.Translate,
    background: Brush = Brush.horizontalGradient(listOf(Color(0xFF14CC9E), Color(0xFF49F2C8))),
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth(),
//            .background(MaterialTheme.colorScheme.secondaryContainer)
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon?.let {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 16.dp)
                            .size(36.dp),
//                tint = MaterialTheme.colorScheme.secondary
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (icon == null) 12.dp else 0.dp, end = 12.dp)
                ) {
                    with(MaterialTheme) {

                        Text(
                            text = title,
                            maxLines = 1,
                            style = typography.titleLarge.copy(fontSize = 20.sp),
//                    color = colorScheme.onSecondaryContainer
                            color = Color.White
                        )
                        if (description != null)
                            Text(
                                text = description,
//                        color = colorScheme.onSecondaryContainer,
                                color = Color.White,
                                maxLines = 2, overflow = TextOverflow.Ellipsis,
                                style = typography.bodyMedium,
                            )
                    }
                }
            }
        }
    }
}