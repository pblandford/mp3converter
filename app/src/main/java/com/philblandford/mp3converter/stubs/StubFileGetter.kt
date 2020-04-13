package com.philblandford.mp3converter.stubs

import android.content.Context
import android.net.Uri
import com.philblandford.mp3convertercore.FileGetter
import com.philblandford.mp3convertercore.MediaFileDescr
import com.philblandford.mp3convertercore.OutputFileDescr
import com.philblandford.mp3convertercore.api.ExportType
import java.io.File
import java.io.FileOutputStream

class StubFileGetter(val context: Context) :
    FileGetter {

  override fun getMidiFiles(): List<MediaFileDescr> {
    return listOf(
        MediaFileDescr(
            0,
            "One.mid",
            Uri.EMPTY
        ),
        MediaFileDescr(
            0,
            "Two.mid",
            Uri.EMPTY
        ),
        MediaFileDescr(
            0,
            "Three.mid",
            Uri.EMPTY
        )
      )
  }

  override fun createNewFile(name: String, type: ExportType): OutputFileDescr? {
    val file = File(context.cacheDir, "tmpfile")
    return OutputFileDescr(
        Uri.EMPTY,
        "",
        FileOutputStream(file)
    )
  }

  override fun registerListener(listener: (List<MediaFileDescr>) -> Unit) {
    TODO("Not yet implemented")
  }

  override fun export(srcUri: Uri, dstUri: Uri) {
    TODO("Not yet implemented")
  }

  override fun getMidiFileDescr(uri: Uri): MediaFileDescr? {
    return null
  }

  override fun finishSave(uri: Uri) {
  }

  override fun deleteFile(uri: Uri) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun getConvertedFiles(): List<MediaFileDescr> {
    TODO("Not yet implemented")
  }
}