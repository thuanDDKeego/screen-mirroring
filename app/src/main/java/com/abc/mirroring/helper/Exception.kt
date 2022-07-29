package com.abc.mirroring.helper

import timber.log.Timber


fun String?.addPrefix() = "<> $this"

sealed class ActionState {
    object ActionComplete : ActionState()
    class ActionError(val e: Exception) : ActionState()
}

// use ---------------------------------------------------------------------------------------------

/**
 * if error appear, do nothing
 * */
inline fun tryOnly(crossinline mayErrorWork: () -> Unit) = try {
    mayErrorWork()
} catch (e: Exception) {
    Timber.d(e)
}

inline fun mayError(
    crossinline mayErrorWork: () -> Unit,
): ActionState {
    try {
        mayErrorWork()
    } catch (e: Exception) {
        Timber.d(e)
        return ActionState.ActionError(e)
    }

    return ActionState.ActionComplete
}

/**
 * if error appear, print to console
 * */
inline fun <T : Any> T.logIfError(
    isLog: Boolean = true,
    tag: String? = javaClass.simpleName.toString(),
    crossinline mayErrorWork: () -> Unit,
): ActionState {
    try {
        mayErrorWork()
    } catch (e: Exception) {
        if (isLog) Timber.d(e)
        return ActionState.ActionError(e)
    }

    return ActionState.ActionComplete
}

// extension ---------------------------------------------------------------------------------------

inline fun ActionState.onComplete(crossinline onIfComplete: () -> Unit): ActionState {

    if (this is ActionState.ActionComplete) {
        onIfComplete()
    }
    return this
}

inline fun ActionState.onError(crossinline onIfError: (e: Exception) -> Unit): ActionState {

    if (this is ActionState.ActionError) {
        onIfError(this.e)
    }
    return this
}

// exception ---------------------------------------------------------------------------------------

class PermissionDeniedException : Exception() {
    override val message: String
        get() = "display-over-other-app permission IS NOT granted!"
}

class NullViewException(private val inputMessage: String?) : Exception() {
    override val message: String?
        get() = inputMessage
}




