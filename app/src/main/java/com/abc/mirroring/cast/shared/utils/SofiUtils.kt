package com.abc.mirroring.cast.shared.utils

import timber.log.Timber

object SofiUtils {

}

inline fun _try(message: String, action: () -> Unit) {
    try {
        action()
    } catch (t: Throwable) {
        Timber.w("Failed to $message. ${t.message}", t)
    }
}