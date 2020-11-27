package com.philblandford.mp3converter.stubs

import com.philblandford.mp3convertercore.Converter
import com.philblandford.mp3convertercore.MediaFileDescr
import com.philblandford.mp3convertercore.api.ExportType
import com.philblandford.mp3convertercore.engine.Settings
import java.io.OutputStream

class StubConverter : Converter {
  override suspend fun convertFile(midiFile: MediaFileDescr, exportType: ExportType, outputStream: OutputStream,
                                   settings:Settings,
                                   updateProgress:(Int)->Unit) {

  }

  override fun cancel() {

  }
}