package com.philblandford.mp3converter.ui.conversion

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.philblandford.mp3convertercore.Converter
import com.philblandford.mp3convertercore.FileGetter
import com.philblandford.mp3convertercore.MediaFileDescr
import com.philblandford.mp3convertercore.api.ExportType
import com.philblandford.mp3convertercore.engine.Settings
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception
import java.util.concurrent.CancellationException

enum class Status { INACTIVE, IN_PROGRESS, COMPLETED }
data class ConvertStatus(val progress: Int, val status: Status, val exception: Exception? = null)
data class SliderRange(val start:Int, val end:Int, val step:Int)

class ConversionViewModel : ViewModel(), KoinComponent {

  private val converter: Converter by inject()
  private val fileGetter: FileGetter by inject()


  var midiFileDescr: MediaFileDescr? = null
  var outputPath: String? = null
  var outputUri: Uri? = null
  private val settings = MutableLiveData(Settings())
  private val exportType = MutableLiveData(ExportType.MP3)
  private val mediator = MediatorLiveData<ExportType>().apply {
    addSource(exportType) { value ->
      setValue(value)
      settings.value?.sampleRate?.let { sr ->
        if (sr > sampleValues.end) {
          setSampleRate(sampleValues.end)
        }
      }
    }
  }.also { it.observeForever { /* empty */ } }
  private val status = MutableLiveData<ConvertStatus>()
  private var convertJob: Job? = null

  val bitRateValues = SliderRange(64,320,64)
  val bitDepthValues = SliderRange(16,32,8)
  val sampleValues
    get() = if (exportType.value == ExportType.MP3) {
       SliderRange(8000, 48000, 8000)
    } else {
      SliderRange(24000, 96000, 8000)
    }

  init {
    clear()
  }

  fun convertFile() {
    if (status.value?.status == Status.INACTIVE) {
      midiFileDescr?.let { mfd ->
        convertJob = viewModelScope.launch {
          try {
            fileGetter.createNewFile(mfd.name, exportType())?.let { outputDescr ->
              updateStatus(Status.IN_PROGRESS)
              outputPath = outputDescr.displayPath
              outputUri = outputDescr.uri
              converter.convertFile(mfd, exportType(),
                outputDescr.outputStream, settings.value
                ?: Settings()) {
                ensureActive()
                postProgress(it)
              }
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

  fun exportFile(destUri: Uri) {
    outputUri?.let {
      fileGetter.export(it, destUri)
    }
  }

  fun getProgressData(): LiveData<ConvertStatus> = status

  fun getSettings():LiveData<Settings> = settings

  fun getExportType():LiveData<ExportType> = exportType

  fun setType(mp3ButtonChecked: Boolean) {
    exportType.postValue(if (mp3ButtonChecked) ExportType.MP3 else ExportType.WAV)
  }

  fun setSampleRate(rate:Int) {
    settings.value?.let {
      settings.postValue(it.copy(sampleRate = rate))
    }
  }

  fun setBitDepth(depth:Int) {
    settings.value?.let {
      settings.postValue(it.copy(bitDepth = depth))
    }
  }

  fun setBitRate(rate:Int) {
    settings.value?.let {
      settings.postValue(it.copy(bitRate = rate))
    }
  }

  fun clear() {
    status.value = ConvertStatus(0, Status.INACTIVE, null)
  }

  fun cancel() {
    convertJob?.cancel(CancellationException("Request to cancel"))
    converter.cancel()
  }

  fun getStatus(): LiveData<ConvertStatus> = status

  private fun exportType() = exportType.value ?: ExportType.MP3

  private fun postProgress(progress: Int) {
    status.value?.let {
      status.postValue(it.copy(progress = progress))
    }
  }

  private fun postException(exception: Exception) {
    status.value?.let {
      Log.e("CVM", "exception", exception)
      status.postValue(it.copy(exception = exception))
    }
  }

  private fun updateStatus(statusVal: Status) {
    status.value?.let {
      status.postValue(it.copy(status = statusVal))
    }
  }
}