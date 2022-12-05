package com.abc.mirroring.cast.setup.graphs

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph
@NavGraph
annotation class ImageNavGraph(
    val start: Boolean = false
)