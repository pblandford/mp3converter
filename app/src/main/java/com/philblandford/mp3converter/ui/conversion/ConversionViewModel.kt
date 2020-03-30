package com.philblandford.mp3converter.ui.conversion

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.philblandford.mp3converter.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception


class ConversionViewModel : ViewModel(), KoinComponent {

  private val converter: Converter by inject()
  private val fileGetter: FileGetter by inject()

  var midiFileDescr:MidiFileDescr? = null
  var path: String? = null
  var uri:Uri? = null
  var exportType:ExportType = ExportType.MP3

  fun convertFile(
    updateProgress: (Int) -> Unit,
    onComplete: () -> Unit, onFailure: (Throwable) -> Unit
  ) {
    midiFileDescr?.let { mfd ->
      GlobalScope.launch {
        try {
          fileGetter.createNewFile(mfd.name, exportType)
            ?.let { outputDescr ->
              path = outputDescr.displayPath
              uri = outputDescr.uri
              converter.convertFile(mfd, exportType, outputDescr.outputStream, updateProgress)
              fileGetter.finishSave(outputDescr.uri)
              onComplete()
            } ?: run {
            throw Exception("Could not create file ${mfd.name}")
          }
        } catch (e: Exception) {
          onFailure(e)
        }
      }
    }
  }

  fun setType(mp3ButtonChecked:Boolean) {
    exportType = if (mp3ButtonChecked) ExportType.MP3 else ExportType.WAV
  }
}