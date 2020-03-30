package com.philblandford.mp3converter

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.OutputStream

enum class ExportType {
  MP3,
  WAV,
  MIDI
}

enum class DestinationType {
  LOCAL,
  SHARE
}

@Parcelize
data class ConvertOptions(
  val exportType: ExportType, val destinationType: DestinationType,
  val midiFile: MidiFileDescr
) : Parcelable

@Parcelize
data class MidiFileDescr(val id: Long, val name: String, val uri: Uri) : Parcelable

data class OutputFileDescr(val uri: Uri, val displayPath: String, val outputStream: OutputStream)

interface FileGetter {
  fun getMidiFiles(): List<MidiFileDescr>
  fun createNewFile(name: String, type: ExportType): OutputFileDescr?
  fun finishSave(uri: Uri)
  fun deleteFile(uri: Uri)
}

interface Converter {
  suspend fun convertFile(
    midiFile: MidiFileDescr, exportType: ExportType, outputStream: OutputStream,
    updateProgress: (Int) -> Unit
  )
}