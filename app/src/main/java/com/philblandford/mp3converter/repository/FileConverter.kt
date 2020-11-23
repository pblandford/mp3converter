package com.philblandford.mp3converter.repository

import android.content.ContentResolver
import com.philblandford.mp3converter.ui.report.reportError
import com.philblandford.mp3convertercore.Converter
import com.philblandford.mp3convertercore.MediaFileDescr
import com.philblandford.mp3convertercore.api.ExportType
import com.philblandford.mp3convertercore.api.MidiToMp3Converter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import java.io.FileInputStream
import java.io.OutputStream

class FileConverter(private val contentResolver: ContentResolver,
                    private val soundFontPath: String) : Converter, KoinComponent {

  private val converter =
      MidiToMp3Converter(soundFontPath)

  override suspend fun convertFile(
      midiFile: MediaFileDescr,
      exportType: ExportType,
      outputStream: OutputStream,
      updateProgress: (Int) -> Unit
  ) {
    withContext(Dispatchers.IO) {
      contentResolver.openFileDescriptor(midiFile.uri, "r")?.use { pfd ->
        val fis = FileInputStream(pfd.fileDescriptor)
        converter.convert(fis, outputStream, exportType, updateProgress) { bytes, e ->
          reportError(bytes, midiFile.name)
          throw(e)
        }
      }
    }
  }

  override fun cancel() {
    converter.cancel()
  }
}