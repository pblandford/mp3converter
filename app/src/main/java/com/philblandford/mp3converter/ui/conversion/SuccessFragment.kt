package com.philblandford.mp3converter.ui.conversion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jean.jcplayer.model.JcAudio
import com.philblandford.mp3converter.ExportType
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FragmentSuccessBinding
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File


class SuccessFragment : Fragment() {

  private lateinit var binding: FragmentSuccessBinding
  private val viewModel: ConversionViewModel by activityViewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = FragmentSuccessBinding.inflate(inflater)

    viewModel.path?.let { path ->
      val name = FilenameUtils.getBaseName(path)
      binding.textSuccess.text = getString(R.string.convert_success, name, path)
      initPlayer()
    }
    binding.buttonShare.setOnClickListener { share() }
    return binding.root
  }

  override fun onStop() {
    super.onStop()
  }

  private fun backToMain() {
    val action = SuccessFragmentDirections.actionSuccessFragmentToFilePickerFragment()
    findNavController().navigate(action)
  }

  private fun share() {
    val mime = when (viewModel.exportType) {
      ExportType.MP3 -> "audio/mp3"
      ExportType.WAV -> "audio/wav"
      ExportType.MIDI -> "audio/midi"
    }

    viewModel.uri?.let { uri ->
      val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = mime
      }
      startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
      backToMain()
    }
  }

  private fun initPlayer() {
    viewModel.uri?.let { uri ->
      binding.videoView.setVideoURI(uri)
    }
  }
}