package com.abc.mirroring.cast.screen.cast.audible.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.abc.mirroring.cast.section.Audible
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.Streamable

@Composable
fun audibles(
    type: MediaType = MediaType.Video,
    data: List<Streamable>,
    callBack: (Streamable) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(count = if (type == MediaType.Video) 3 else 1),
        state = rememberLazyGridState(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(data, key = { it.id() }) {
            if (type == MediaType.Video) video(media = it as Audible) {
                callBack.invoke(it)
            }
            else audio(media = it as Audible) {
                callBack.invoke(it)
            }
        }
    }
}