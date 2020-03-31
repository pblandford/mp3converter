package com.philblandford.mp3converter.ui.filepicker

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.philblandford.mp3converter.Converter
import com.philblandford.mp3converter.FileGetter
import com.philblandford.mp3converter.MidiFileDescr
import org.koin.core.KoinComponent
import org.koin.core.inject

class FilePickerViewModel : ViewModel(), KoinComponent {

  private val fileGetter:FileGetter by inject()


  fun getFileNames():List<MidiFileDescr> {
    return fileGetter.getMidiFiles().sortedBy { it.name.toLowerCase() }
  }


}