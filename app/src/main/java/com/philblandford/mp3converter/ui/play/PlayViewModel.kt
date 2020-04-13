package com.philblandford.mp3converter.ui.play

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.philblandford.mp3convertercore.FileGetter
import org.koin.core.KoinComponent
import org.koin.core.inject

class PlayViewModel : ViewModel(), KoinComponent {
  val fileGetter: FileGetter by inject()

  fun exportFile(src: Uri, dest:Uri) {
    fileGetter.export(src, dest)
  }
}