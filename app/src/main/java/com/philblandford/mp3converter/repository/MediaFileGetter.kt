package com.philblandford.mp3converter.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.FileObserver
import android.provider.MediaStore
import com.philblandford.mp3convertercore.FileGetter
import com.philblandford.mp3convertercore.MediaFileDescr
import com.philblandford.mp3convertercore.OutputFileDescr
import com.philblandford.mp3convertercore.api.ExportType
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.jetbrains.annotations.TestOnly
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


class MediaFileGetter(
  private val contentResolver: ContentResolver,
  private val context: Context
) : FileGetter {

  private var observer: FileObserver? = null

  override fun getMidiFiles(): List<MediaFileDescr> {
    val media = doQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
    return (media).sortedBy { it.name }
  }

  private fun doQuery(uri: Uri): List<MediaFileDescr> {
    val projection = arrayOf(
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.DISPLAY_NAME,
      MediaStore.Audio.Media.SIZE
    )

    val sortOrder = MediaStore.Audio.Media.DISPLAY_NAME
    val query = contentResolver.query(
      uri,
      projection,
      null,
      null,
      sortOrder
    )
    val results = mutableListOf<MediaFileDescr>()
    query?.use { cursor ->
      val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
      val nameColumn =
        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

      while (cursor.moveToNext()) {
        val id = cursor.getLong(idColumn)
        val name = cursor.getString(nameColumn)
        val baseName = FilenameUtils.getBaseName(name)
        val contentUri = ContentUris.withAppendedId(uri, id)
        results.add(MediaFileDescr(id, baseName, contentUri))
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
    val fullName = "${FilenameUtils.getBaseName(name)}.$extension"

    val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
    val file = File(dir, fullName)
    if (file.exists()) {
      file.delete()
    }
    val output = FileOutputStream(file)
    return OutputFileDescr(Uri.fromFile(file), fullName, output)
  }

  override fun finishSave(uri: Uri) {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.IS_PENDING, 0)
  }

  override fun deleteFile(uri: Uri) {
    contentResolver.delete(uri, null, null)
  }

  override fun getMidiFileDescr(uri: Uri): MediaFileDescr? {

    val arr = arrayOf(MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media._ID)
    val cursor = contentResolver.query(uri, arr, null, null, null)
    return cursor?.let {

      val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
      val idIndex = try {
        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
      } catch (e:Exception) {
        null
      }
      cursor.moveToFirst()
      val name = cursor.getString(columnIndex)
      val id = idIndex?.let {  cursor.getLong(idIndex) } ?: 0
      cursor.close()
      MediaFileDescr(id, name, uri)
    }
  }

  override fun export(srcUri: Uri, dstUri: Uri) {
    val input = contentResolver.openInputStream(srcUri)

    contentResolver.openFileDescriptor(dstUri, "w")?.use {
      FileOutputStream(it.fileDescriptor).use { fos ->
        fos.write(IOUtils.toByteArray(input))
      }
    }
  }

  override fun getConvertedFiles(): List<MediaFileDescr> {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
    return dir?.let {
      getFilesAsDescrs(dir)
    } ?: listOf()
  }

  override fun registerListener(listener: (List<MediaFileDescr>) -> Unit) {
    context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.let { dir ->
      observer?.stopWatching()
      observer = ConvertedFileObserver(dir, listener)
      observer?.startWatching()
    }
  }

  @TestOnly
  fun clear() {
    val files = getMidiFiles()
    files.forEach { deleteFile(it.uri) }

  }

  private fun getFilesAsDescrs(dir: File): List<MediaFileDescr> {
    return dir.list()?.map { fileName ->
      val uri = Uri.fromFile(File(dir, fileName))
      MediaFileDescr(0L, fileName, uri)
    } ?: listOf()
  }

  private inner class ConvertedFileObserver(
    private val dir: File,
    private val listener: (List<MediaFileDescr>) -> Unit
  ) : FileObserver(dir) {
    override fun onEvent(event: Int, path: String?) {
      if (event == CREATE) {
        val files = getFilesAsDescrs(dir)
        listener(files)
      }
    }
  }
}