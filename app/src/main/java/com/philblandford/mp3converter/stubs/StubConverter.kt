package com.philblandford.mp3converter.stubs

import com.philblandford.mp3converter.Converter
import com.philblandford.mp3converter.ExportType
import com.philblandford.mp3converter.MidiFileDescr
import java.io.OutputStream

class StubConverter : Converter {
  override suspend fun convertFile(midiFile: MidiFileDescr, exportType: ExportType, outputStream: OutputStream,
                                   updateProgress:(Int)->Unit) {

  }
}