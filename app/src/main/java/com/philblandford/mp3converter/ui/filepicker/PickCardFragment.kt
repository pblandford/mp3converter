package com.philblandford.mp3converter.ui.filepicker

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FragmentPickCardBinding
import com.philblandford.mp3converter.ui.conversion.ConversionViewModel
import com.philblandford.mp3convertercore.MediaFileDescr
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.lang.reflect.Method
import java.util.regex.Pattern

private const val FILE_PICKER_REQUEST_CODE = 0

class PickCardFragment : DialogFragment() {

  private lateinit var binding: FragmentPickCardBinding
  private val viewModel: ConversionViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    viewModel.midiFileDescr?.let {
      navigateToConvertOptions()
      return null
    } ?: run {
      getExternalStoragePath()?.let { ext ->
        binding = FragmentPickCardBinding.inflate(inflater)
        binding.storageIcon.setOnClickListener {
          openDocTreeMaterial("/")
        }
        binding.sdcardIcon.setOnClickListener {
          openDocTreeMaterial(ext)
        }
        return binding.root
      } ?: run {
        openDocTreeMaterial("/")
        return null
      }
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    return dialog
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
      Log.i("TAG", "${data?.data}")
      data?.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)?.let { fp ->
        val uri = Uri.fromFile(File(fp))
        viewModel.midiFileDescr = MediaFileDescr(0L, FilenameUtils.getBaseName(fp), uri)
        navigateToConvertOptions()
      }
    }
  }

  private fun navigateToConvertOptions() {
    val action =
      PickCardFragmentDirections.actionPickCardFragmentToConvertOptionsFragment()
    findNavController().navigate(action)
  }

  private fun openDocTreeMaterial(path: String) {
    var fp = MaterialFilePicker()
      .withSupportFragment(this)
      .withCloseMenu(true)
      .withFilter(Pattern.compile(".*\\.(mid|midi|MIDI|MID)$"))
      .withFilterDirectories(false)
      .withTitle("Choose a MIDI file..")
      .withRequestCode(FILE_PICKER_REQUEST_CODE)

    fp = if (path == "/") {
      fp.withPath(path)
    } else {
      fp.withRootPath(path)
    }
    fp.start()
  }

  private fun getExternalStoragePath(): String? {
    if (Build.VERSION.SDK_INT >= 24) {
      val storageManager: StorageManager =
        context?.getSystemService(Context.STORAGE_SERVICE) as StorageManager
      val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
      val getPath: Method = storageVolumeClazz.getMethod("getPath")
      return storageManager.storageVolumes.find { it.isRemovable }?.let {
        return getPath.invoke(it) as String
      }
    } else {
      return null
    }
  }
}