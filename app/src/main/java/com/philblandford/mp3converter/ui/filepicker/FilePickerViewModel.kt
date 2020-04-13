package com.philblandford.mp3converter.ui.filepicker

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.philblandford.mp3convertercore.FileGetter
import com.philblandford.mp3convertercore.MediaFileDescr
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class FilePickerViewModel : ViewModel(), KoinComponent {

  private val fileGetter: FileGetter by inject()
  private val convertedFiles = MutableLiveData<List<MediaFileDescr>>()

//  init {
//    fileGetter.registerListener { files ->
//      convertedFiles.postValue(files.sorted())
//    }
//    val files = fileGetter.getConvertedFiles()
//    convertedFiles.postValue(files.sorted())
//  }

  fun getConvertedFileNames(): LiveData<List<MediaFileDescr>> {
    return convertedFiles
  }

  fun getConvertedFilesStatic():List<MediaFileDescr> {
    return fileGetter.getConvertedFiles().sorted()
  }

  fun getMidiDescr(uri: Uri): MediaFileDescr? {
    return fileGetter.getMidiFileDescr(uri)
  }

  private fun List<MediaFileDescr>.sorted() = this.sortedBy { it.name.toUpperCase(Locale.getDefault()) }

}