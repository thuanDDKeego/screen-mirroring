package com.abc.mirroring.cast.section

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import timber.log.Timber

/**
 * Chap nhan hard code giai doan 1.0.0
 */
object MediaPicker {

    fun audible(context: Context, type: MediaType): List<Audible> {
        return when (type) {
            MediaType.Audio -> audios(context)
            MediaType.Video -> videos(context)
            else -> emptyList()
        }
    }

    @SuppressLint("Recycle")
    fun videos(context: Context): List<Audible> = query(
        context,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA,
        )
    ) { root, cursor, idx ->
        if (idx[MediaStore.Video.Media._ID] == null || idx[MediaStore.Video.Media.DATA] == null) Unit/*just need return a type different from Audio */
        else {
            val id = cursor.getLong(idx[MediaStore.Video.Media._ID]!!)
            val name = if (idx[MediaStore.Video.Media.DISPLAY_NAME] != null) cursor.getString(idx[MediaStore.Video.Media.DISPLAY_NAME]!!) else "no name"
            val dateAdded = if (idx[MediaStore.Video.Media.DATE_ADDED] != null) cursor.getInt(idx[MediaStore.Video.Media.DATE_ADDED]!!).toLong() else 0L
            val data = cursor.getString(idx[MediaStore.Video.Media.DATA]!!)
            val size = if (idx[MediaStore.Video.Media.SIZE] != null) cursor.getInt(idx[MediaStore.Video.Media.SIZE]!!) else 0
            val duration = if (idx[MediaStore.Video.Media.DURATION] != null) cursor.getInt(idx[MediaStore.Video.Media.DURATION]!!) else 0
            Audible(
                id,
                name,
                ContentUris.withAppendedId(root, id),
                dateAdded,
                data,
                size,
                duration,
                MediaType.Video
            )
        }
    }

    /**
     * TODO: Dang bi lap giong videos
     */
    @SuppressLint("Recycle")
    fun audios(context: Context): List<Audible> = query(
        context,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
        )
    ) { root, cursor, idx ->
        if (idx[MediaStore.Audio.Media._ID] == null || idx[MediaStore.Audio.Media.DATA] == null) Unit/*just need return a type different from Audio */
        else {
            val id = cursor.getLong(idx[MediaStore.Audio.Media._ID]!!)
            val name = if (idx[MediaStore.Audio.Media.DISPLAY_NAME] != null) cursor.getString(idx[MediaStore.Audio.Media.DISPLAY_NAME]!!) else "no name"
            val dateAdded = if (idx[MediaStore.Audio.Media.DATE_ADDED] != null) cursor.getInt(idx[MediaStore.Audio.Media.DATE_ADDED]!!).toLong() else 0L
            val data = cursor.getString(idx[MediaStore.Audio.Media.DATA]!!)
            val size = if (idx[MediaStore.Audio.Media.SIZE] != null) cursor.getInt(idx[MediaStore.Audio.Media.SIZE]!!) else 0
            val duration = if (idx[MediaStore.Audio.Media.DURATION] != null) cursor.getInt(idx[MediaStore.Audio.Media.DURATION]!!) else 0
            Audible(
                id,
                name,
                ContentUris.withAppendedId(root, id),
                dateAdded,
                data,
                size,
                duration,
                MediaType.Audio
            )
        }
    }

    fun images(context: Context): List<Image> = query(
        context,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
        )
    ) { root, cursor, idx ->
        // Get values of columns for a given video.
        if (idx[MediaStore.Video.Media._ID] == null) Unit/*just need return a type different from Image */
        else {
            val id = cursor.getLong(idx[MediaStore.Video.Media._ID]!!)
            val name = if (idx[MediaStore.Images.Media.DISPLAY_NAME] != null) cursor.getString(idx[MediaStore.Images.Media.DISPLAY_NAME]!!) else "no name!"
            Image(
                id,
                name,
                ContentUris.withAppendedId(root, id),
                cursor.getInt(idx[MediaStore.Images.Media.DATE_ADDED]!!).toLong(),
            )
        }
    }

    /**
     * TODO: [high] Replace this function
     */
    fun getImage(context: Context, video: Audible): Bitmap? {
        return MediaStore.Video.Thumbnails.getThumbnail(
            context.contentResolver,
            video.id,
            MediaStore.Video.Thumbnails.MINI_KIND,
            null
        )
    }


    private inline fun <reified T> query(context: Context, root: Uri, projection: Array<String>, onMapping: (Uri, Cursor, Map<String, Int>) -> Any): List<T> {
        val cursor: Cursor = context.contentResolver?.query(root, projection, null, null, null) ?: return emptyList()

        val idx = projection.associateWith { cursor.getColumnIndex(it) }

        val medias = mutableListOf<T>()
        while (cursor.moveToNext()) {
            // Get values of columns for a given video.
            val media = onMapping(root, cursor, idx)
            if (media is T) medias.add(media)
        }

        Timber.d("Fetched $medias")

        return medias
    }

}