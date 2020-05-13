package com.philblandford.mp3converter.ui.play

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.philblandford.mp3convertercore.api.ExportType
import com.philblandford.mp3converter.databinding.FragmentPlayBinding
import org.apache.commons.io.FilenameUtils


class PlayFragment : PlayFragmentBase() {

  private val args: PlayFragmentArgs by navArgs()
  private lateinit var binding: FragmentPlayBinding
  private val viewModel: PlayViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentPlayBinding.inflate(inflater)
    initPlayer()
    getFileName()?.let { name ->
      binding.textSuccess.text = name
      initPlayer()
    }
    binding.buttonShare.setOnClickListener { share() }
    binding.buttonSave.setOnClickListener { save() }
    return binding.root
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    return dialog
  }

  override fun backToMain() {
    val action = PlayFragmentDirections.actionFilePlayFragmentToFilePickerFragment()
    findNavController().navigate(action)
  }

  override fun getOutputUri(): Uri? {
    return args.desc.uri
  }

  override fun getExportType(): ExportType {
    return when (FilenameUtils.getExtension(args.desc.uri.path)) {
      "mp3" -> ExportType.MP3
      "wav" -> ExportType.WAV
      else -> ExportType.MP3
    }
  }

  override fun getFileName(): String? {
    return FilenameUtils.getBaseName(args.desc.name)
  }

  override fun exportFile(dest: Uri) {
    getOutputUri()?.let { src ->

      viewModel.exportFile(src, dest)
    }
  }

  private fun initPlayer() {
    getOutputUri()?.let { uri ->
      binding.videoView.setVideoURI(uri)
      binding.videoView.videoControls?.setCanHide(false)
    }
  }


}