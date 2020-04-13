package com.philblandford.mp3converter.ui.play

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.DialogFragment
import com.philblandford.mp3converter.R
import com.philblandford.mp3convertercore.api.ExportType


abstract class PlayFragmentBase : DialogFragment() {

  abstract fun backToMain()
  abstract fun getOutputUri(): Uri?
  abstract fun getExportType(): ExportType
  abstract fun getFileName(): String?
  abstract fun exportFile(dest: Uri)

  protected fun share() {

    getOutputUri()?.let { src ->
      val packageName = activity?.packageName
      val apkURI = FileProvider.getUriForFile(activity?.applicationContext!!, "$packageName.provider", src.toFile())

      val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, apkURI)
        type = getMime()
      }
      startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    backToMain()
    super.onDismiss(dialog)
  }

  protected fun save() {
    val exportType = getExportType()
    val ext = if (exportType == ExportType.MP3) "mp3" else "wav"
    val fullName = "${getFileName()}.$ext"
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = if (exportType == ExportType.MP3) "audio/mp3" else "audio/wav"
      putExtra(Intent.EXTRA_TITLE, fullName)
    }
    startActivityForResult(intent, 0)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data); if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

      Log.i("TAG", "${data?.data}")
      data?.data?.let { uri ->
        exportFile(uri)
      }
    }
  }

  private fun getMime(): String {
    return when (getExportType()) {
      ExportType.MP3 -> "audio/mp3"
      ExportType.WAV -> "audio/wav"
      ExportType.MIDI -> "audio/midi"
    }
  }
}