package net.sofigo.cast.tv.shared.cast

sealed class Command {
    object Pause : Command()
    object Previous : Command()
    object Next : Command()

    object Replay10 : Command()
    object Forward10 : Command()
    object VolumeUp : Command()
    object VolumeDown : Command()
    object Play : Command()
    object Stop : Command()
    object Mute : Command()
    data class Seek(val position: Float) : Command()
}