package com.abc.mirroring.cast.screen.cast.iptv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abc.mirroring.R
import com.abc.mirroring.cast.section.data.iptv.M3U

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun dialog_iptv_url(
    onHide: () -> Unit,
    onAdd: (M3U) -> Unit
) {
    var iptvAddress by remember { mutableStateOf("") }
    var iptvName by remember { mutableStateOf("") }
    //check if invalid address, show warning text
    var invalidAdrress by remember { mutableStateOf(false) }
    //name must not be empty
    var invalidName by remember { mutableStateOf(false) }
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            onHide.invoke()
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
            Text(
                text = stringResource(id = R.string.add_iptv),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
            )
//            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(id = R.string.add_iptv),
                fontSize = 18.sp,
                textAlign = TextAlign.Start,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            _custom_edit_text(
                hint = stringResource(id = R.string.enter_address_here)
            ) { value ->
                invalidAdrress = false
                iptvAddress = value

            }
            //if invalid adrress, show warning red text
            if (invalidAdrress) {
                Text(
                    text = stringResource(id = R.string.invalid_iptv_address),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.iptv_name),
                fontSize = 18.sp,
                textAlign = TextAlign.Start,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            _custom_edit_text(
                hint = stringResource(id = R.string.enter_name_here)
            ) { value ->
                iptvName = value
                invalidName = false
            }
            if (invalidName) {
                Text(
                    text = stringResource(id = R.string.name_must_not_be_empty),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE))
                    .clickable { onHide.invoke() }
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
                    .background(Color(0xFF0091EA))
                    .clickable {
                        if(!iptvAddress.startsWith("http://", ignoreCase = true) && !iptvAddress.startsWith("https://", ignoreCase = true)) {
                            invalidAdrress = true
                            return@clickable
                        }
                        if(iptvName.trim().isEmpty()) {
                            invalidName = true
                            return@clickable
                        }
                        onHide()
                        onAdd.invoke(M3U(iptvName, iptvAddress))
                    }
                    .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.add), textAlign = TextAlign.Center, color = Color.White, fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        }
    }
}

