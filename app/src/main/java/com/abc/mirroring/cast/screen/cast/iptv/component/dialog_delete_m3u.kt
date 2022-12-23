package com.abc.mirroring.cast.screen.cast.iptv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abc.mirroring.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun dialog_delete_m3u(
    title: String,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            onCancel.invoke()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
//            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text(
                text = stringResource(id = R.string.are_you_sure_you_want_to_delete),
                fontSize = 18.sp,
                textAlign = TextAlign.Start,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE))
                    .clickable { onCancel.invoke() }
                    .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel), textAlign = TextAlign.Center, color = Color.Black, fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                Box(modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Red)
                    .clickable {
                        onDelete.invoke()
                    }
                    .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete), textAlign = TextAlign.Center, color = Color.White, fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        }
    }
}