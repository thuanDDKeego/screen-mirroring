package com.abc.mirroring.cast.screen.cast.iptv

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abc.mirroring.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun dialog_iptv_url(
    onHide: () -> Unit,
    onAdd: () -> Unit
) {
    val iptvAddress = remember { mutableStateOf("") }
    val iptvName = remember { mutableStateOf("") }
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
            _custom_editText(
                value = iptvAddress,
                hint = stringResource(id = R.string.enter_address_here)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.add_iptv),
                fontSize = 18.sp,
                textAlign = TextAlign.Start,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            _custom_editText(
                value = iptvName,
                hint = stringResource(id = R.string.enter_name_here)
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
                        onHide.invoke()
                        onAdd.invoke()
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

@Composable
fun _custom_editText(
    hint: String = "",
    value: MutableState<String>
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .background(shape = RoundedCornerShape(10.dp), color = Color.White)
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value.value,
            onValueChange = { value.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 36.dp),
            singleLine = true,
            textStyle = TextStyle(textAlign = TextAlign.Left),
            keyboardActions = KeyboardActions(
                onDone = {
                    //clear focus on text-field
                    focusManager.clearFocus()
                }
            ),
            decorationBox = { innerTextField ->
                if (value.value.isEmpty()) Text(
                    text = hint,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                )
                innerTextField()
            }
        )
    }
}
