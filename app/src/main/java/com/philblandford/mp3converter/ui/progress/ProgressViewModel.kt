package com.philblandford.mp3converter.ui.progress

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.philblandford.mp3converter.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception


class ProgressViewModel : ViewModel(), KoinComponent {

  private val converter: Converter by inject()
  private val fileGetter: FileGetter by inject()

  var path: String? = null
  var uri:Uri? = null

  fun convertFile(
    convertOptions: ConvertOptions,
    updateProgress: (Int) -> Unit,
    onComplete: () -> Unit, onFailure: (Throwable) -> Unit
  ) {
    GlobalScope.launch {
      try {
        fileGetter.createNewFile(convertOptions.midiFile.name, convertOptions.exportType)
          ?.let { outputDescr ->
            path = outputDescr.displayPath
            uri = outputDescr.uri
            converter.convertFile(
              convertOptions.midiFile, convertOptions.exportType, outputDescr.outputStream,
              updateProgress
            )
            fileGetter.finishSave(outputDescr.uri)
            onComplete()
          } ?: run {
          throw Exception("Could not create file ${convertOptions.midiFile.name}")
        }
      } catch (e: Exception) {
        onFailure(e)
      }
    }
  }

   fun deleteFile() {
     uri?.let {
       fileGetter.deleteFile(it)
     }
   }
}