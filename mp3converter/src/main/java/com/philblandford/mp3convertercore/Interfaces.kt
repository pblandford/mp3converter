package com.philblandford.mp3convertercore

import android.net.Uri
import android.os.Parcelable
import com.philblandford.mp3convertercore.api.ExportType
import kotlinx.android.parcel.Parcelize
import java.io.OutputStream

enum class DestinationType {
  LOCAL,
  SHARE
}

@Parcelize
data class ConvertOptions(val exportType: ExportType, val midiFile: MediaFileDescr) : Parcelable

@Parcelize
data class MediaFileDescr(val id: Long, val name: String, val uri: Uri) : Parcelable

data class OutputFileDescr(val uri: Uri, val displayPath: String, val outputStream: OutputStream)

interface FileGetter {
  fun getMidiFiles(): List<MediaFileDescr>
  fun getConvertedFiles(): List<MediaFileDescr>
  fun registerListener(listener:(List<MediaFileDescr>)->Unit)
  fun createNewFile(name: String, type: ExportType): OutputFileDescr?
  fun finishSave(uri: Uri)
  fun deleteFile(uri: Uri)
  fun getMidiFileDescr(uri: Uri): MediaFileDescr?
  fun export(srcUri:Uri, dstUri:Uri)
}

interface Converter {
  suspend fun convertFile(
    midiFile: MediaFileDescr, exportType: ExportType, outputStream: OutputStream,
    updateProgress: (Int) -> Unit
  )
  fun cancel()
}