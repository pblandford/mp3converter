package com.philblandford.mp3converter.stubs

import android.content.Context
import android.net.Uri
import com.philblandford.mp3converter.ExportType
import com.philblandford.mp3converter.FileGetter
import com.philblandford.mp3converter.MidiFileDescr
import com.philblandford.mp3converter.OutputFileDescr
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class StubFileGetter(val context: Context) : FileGetter {

  override fun getMidiFiles(): List<MidiFileDescr> {
    return listOf(MidiFileDescr(0, "One.mid", Uri.EMPTY),
      MidiFileDescr(0, "Two.mid", Uri.EMPTY),
      MidiFileDescr(0, "Three.mid", Uri.EMPTY)
      )
  }

  override fun createNewFile(name: String, type: ExportType): OutputFileDescr? {
    val file = File(context.cacheDir, "tmpfile")
    return OutputFileDescr(Uri.EMPTY, "", FileOutputStream(file))
  }

  override fun finishSave(uri: Uri) {
  }

  override fun deleteFile(uri: Uri) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}