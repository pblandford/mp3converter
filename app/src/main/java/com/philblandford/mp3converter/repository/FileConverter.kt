package com.philblandford.mp3converter.repository

import android.content.ContentResolver
import android.util.Log
import com.philblandford.mp3converter.ui.report.reportError
import com.philblandford.mp3convertercore.Converter
import com.philblandford.mp3convertercore.MediaFileDescr
import com.philblandford.mp3convertercore.api.ExportType
import com.philblandford.mp3convertercore.api.MidiToMp3Converter
import com.philblandford.mp3convertercore.engine.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.koin.core.KoinComponent
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

class FileConverter(
    private val contentResolver: ContentResolver,
    soundFontPath: String
) : Converter, KoinComponent {

  private val converter =
    MidiToMp3Converter(soundFontPath)

  override suspend fun convertFile(
    midiFile: MediaFileDescr,
    exportType: ExportType,
    outputStream: OutputStream,
    settings: Settings,
    updateProgress: (Int) -> Unit
  ) {
    withContext(Dispatchers.IO) {
      val inputStream = when (midiFile.uri.scheme) {
          "content" -> contentResolver.openInputStream(midiFile.uri)
          "file" -> FileUtils.openInputStream(File(midiFile.uri.path))
        else -> null
      }
      inputStream?.let {
        converter.convert(
            inputStream,
            outputStream,
            exportType,
          settings,
            updateProgress
        ) { bytes, e ->
          reportError(bytes, midiFile.name, e)
          throw(e)
        }
      } ?: run {
        Log.e("File", "Could not open inputStrem")
      }
    }
  }

  override fun cancel() {
    converter.cancel()
  }
}