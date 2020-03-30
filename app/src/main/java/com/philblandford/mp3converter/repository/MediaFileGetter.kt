package com.philblandford.mp3converter.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import com.philblandford.mp3converter.ExportType
import com.philblandford.mp3converter.FileGetter
import com.philblandford.mp3converter.MidiFileDescr
import com.philblandford.mp3converter.OutputFileDescr
import org.apache.commons.io.FilenameUtils
import org.jetbrains.annotations.TestOnly
import java.io.OutputStream
import java.util.*


class MediaFileGetter(private val contentResolver: ContentResolver) : FileGetter {

  override fun getMidiFiles(): List<MidiFileDescr> {
    val downloads = doQuery(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
    val media = doQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
    return (downloads + media).sortedBy { it.name }
  }

  private fun doQuery(uri: Uri): List<MidiFileDescr> {
    val projection = arrayOf(
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.DISPLAY_NAME,
      MediaStore.Audio.Media.SIZE
    )

    val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} like \"%mid\""

    val sortOrder = MediaStore.Audio.Media.DISPLAY_NAME
    val query = contentResolver.query(
      uri,
      projection,
      selection,
      null,
      sortOrder
    )
    val results = mutableListOf<MidiFileDescr>()
    query?.use { cursor ->
      val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
      val nameColumn =
        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

      while (cursor.moveToNext()) {
        val id = cursor.getLong(idColumn)
        val name = cursor.getString(nameColumn)
        val baseName = FilenameUtils.getBaseName(name)
        val contentUri = ContentUris.withAppendedId(uri, id)
        results.add(MidiFileDescr(id, baseName, contentUri))
      }
    }
    return results
  }

  override fun createNewFile(name: String, type: ExportType): OutputFileDescr? {
    val extension = when (type) {
      ExportType.MP3 -> "mp3"
      ExportType.WAV -> "wav"
      ExportType.MIDI -> "mid"
    }
    val fullName = "$name.$extension"
    val values = ContentValues().apply {
      put(MediaStore.Audio.Media.DISPLAY_NAME, fullName)
      put(MediaStore.Audio.Media.MIME_TYPE, "audio/${type.name.toLowerCase(Locale.ENGLISH)}")
      put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/MidiToMP3Converter/")
      put(MediaStore.Audio.Media.IS_PENDING, 1)
    }

    val path = "/Music/MidiToMP3Converter/$name.$extension"
    val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    return contentResolver.insert(collection, values)?.let { uri ->
      contentResolver.openOutputStream(uri)?.let { os -> OutputFileDescr(uri, path, os) }
    }
  }

  override fun finishSave(uri: Uri) {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.IS_PENDING, 0)
    contentResolver.update(uri, values, null, null)
  }

  override fun deleteFile(uri: Uri) {
    contentResolver.delete(uri, null, null)
  }

  @TestOnly
  fun clear() {
    val files = getMidiFiles()
    files.forEach { deleteFile(it.uri) }

  }
}