package com.abc.mirroring.cast

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * Represents a base type for the state of a UI component
 */
interface State {

}

/**
 * Base for all the ViewModels
 */
open class VimelStateHolder<STATE : State>(initial: STATE) : ViewModel() {

    /**
     * Mutable State of this ViewModel
     */
    private val _state = MutableStateFlow(initial)

    /**
     * State to be exposed to the UI layer
     */
    val state: StateFlow<STATE> = _state.asStateFlow()

    /**
     * Retrieves the current UI state
     */
    val currentState: STATE get() = state.value

    /**
     * Updates the state of this ViewModel and returns the new state
     *
     * @param update Lambda callback with old state to calculate a new state
     * @return The updated state
     */
    protected fun update(update: (old: STATE) -> STATE): STATE =
        _state.updateAndGet(update)

    fun <T : State> stateOrPreview(default: T, onGetFailed: () -> Unit = {}): StateFlow<T> {
        return if (currentState is PreviewState) {
            return MutableStateFlow(default)
        } else {
            try {
                (this as VimelStateHolder<T>).state
            } catch (e: Exception) {
                onGetFailed()
                return MutableStateFlow(default)
            }
        }
    }
}