package com.philblandford.mp3converter.ui.conversion

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.philblandford.mp3converter.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception

enum class Status { INACTIVE, IN_PROGRESS, COMPLETED }
data class ConvertStatus(val progress: Int, val status: Status, val failure: Exception? = null)

class ConversionViewModel : ViewModel(), KoinComponent {

  private val converter: Converter by inject()
  private val fileGetter: FileGetter by inject()

  var midiFileDescr: MidiFileDescr? = null
  var path: String? = null
  var uri: Uri? = null
  var exportType: ExportType = ExportType.MP3
  private val status = MutableLiveData<ConvertStatus>()
  private var convertJob: Job? = null

  init {
    clear()
  }

  fun convertFile() {
    if (status.value?.status == Status.INACTIVE) {
      midiFileDescr?.let { mfd ->
        convertJob = viewModelScope.launch {
          try {
            fileGetter.createNewFile(mfd.name, exportType)?.let { outputDescr ->
              updateStatus(Status.IN_PROGRESS)
              path = outputDescr.displayPath
              uri = outputDescr.uri
              converter.convertFile(mfd, exportType, outputDescr.outputStream) { postProgress(it) }
              fileGetter.finishSave(outputDescr.uri)
              updateStatus(Status.COMPLETED)
            } ?: run {
              throw Exception("Could not create file ${mfd.name}")
            }
          } catch (e: Exception) {
            postException(e)
          }
        }
      }
    }
  }

  fun getProgressData(): LiveData<ConvertStatus> = status

  fun setType(mp3ButtonChecked: Boolean) {
    exportType = if (mp3ButtonChecked) ExportType.MP3 else ExportType.WAV
  }

  fun clear() {
    status.value = ConvertStatus(0, Status.INACTIVE, null)
  }

  private fun postProgress(progress: Int) {
    status.value?.let {
      status.postValue(it.copy(progress = progress))
    }
  }

  private fun postException(exception: Exception) {
    status.value?.let {
      status.postValue(it.copy(failure = exception))
    }
  }

  private fun updateStatus(statusVal: Status) {
    status.value?.let {
      status.postValue(it.copy(status = statusVal))
    }
  }
}