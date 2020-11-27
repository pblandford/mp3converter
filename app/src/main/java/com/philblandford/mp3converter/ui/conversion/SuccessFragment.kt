package com.philblandford.mp3converter.ui.conversion

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.philblandford.mp3converter.R
import com.philblandford.mp3convertercore.api.ExportType
import com.philblandford.mp3converter.databinding.FragmentPlayBinding
import com.philblandford.mp3converter.ui.play.PlayFragmentBase
import org.apache.commons.io.FilenameUtils


class SuccessFragment : PlayFragmentBase() {

  private lateinit var binding: FragmentPlayBinding
  private val viewModel: ConversionViewModel by activityViewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = FragmentPlayBinding.inflate(inflater)

    viewModel.outputPath?.let { path ->
      val name = FilenameUtils.getBaseName(path)
      binding.textSuccess.text = getString(R.string.convert_success, name)
      initPlayer()
    }
    binding.buttonShare.setOnClickListener { share() }
    binding.buttonSave.setOnClickListener { save() }
    return binding.root
  }

  override fun backToMain() {
    val action = SuccessFragmentDirections.actionSuccessFragmentToFilePickerFragment()
    findNavController().navigate(action)
  }

  override fun getOutputUri(): Uri? {
    return viewModel.outputUri
  }

  override fun getExportType(): ExportType {
    return viewModel.getExportType().value ?: ExportType.MP3
  }

  override fun getFileName(): String? {
    return FilenameUtils.getBaseName(viewModel.midiFileDescr?.name)
  }

  override fun exportFile(dest: Uri) {
    viewModel.exportFile(dest)
  }

  private fun initPlayer() {
    getOutputUri()?.let { uri ->
      binding.videoView.setVideoURI(uri)
      binding.videoView.videoControls?.setCanHide(false)
    }
  }


}