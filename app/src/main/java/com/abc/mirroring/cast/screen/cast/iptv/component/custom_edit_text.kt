package com.abc.mirroring.cast.screen.cast.iptv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun _custom_edit_text(
    modifier: Modifier = Modifier,
    hint: String = "",
    onChange: (String) -> Unit
) {
    var value by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .background(shape = RoundedCornerShape(10.dp), color = Color.White)
            .height(36.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = {
                value = it
                onChange(it)
            },
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
                if (value.isEmpty()) Text(
                    text = hint,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                )
                innerTextField()
            }
        )
    }
}