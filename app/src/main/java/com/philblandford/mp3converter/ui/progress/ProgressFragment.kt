package com.philblandford.mp3converter.ui.progress

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.philblandford.mp3converter.DestinationType
import com.philblandford.mp3converter.ExportType
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FragmentProgressBinding


class ProgressFragment : DialogFragment() {

  private val args: ProgressFragmentArgs by navArgs()
  private lateinit var binding: FragmentProgressBinding
  private val viewModel: ProgressViewModel by viewModels()

  override fun onResume() {
    super.onResume()
    viewModel.convertFile(args.convertOptions,
      { binding.progressBar.setProgress(it, false) }, ::complete
    ) {
      activity?.runOnUiThread {
        Toast.makeText(
          context,
          getString(R.string.convert_failed, args.convertOptions.midiFile.name, it.message),
          Toast.LENGTH_LONG
        ).show()
      }
    }
  }

  private fun complete() {
    when (args.convertOptions.destinationType) {
      DestinationType.LOCAL -> showSuccess()
      DestinationType.SHARE -> share()
    }
  }

  private fun showSuccess() {
    val path = viewModel.path ?: "Unknown"
    val action = ProgressFragmentDirections.actionProgressFragmentToBlankFragment(path)
    findNavController().navigate(action)
  }

  private fun share() {
    val mime = when (args.convertOptions.exportType) {
      ExportType.MP3 -> "audio/mp3"
      ExportType.WAV -> "audio/wav"
      ExportType.MIDI -> "audio/midi"
    }

    viewModel.uri?.let { uri ->
      val shareIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, viewModel.uri)
        type = mime
      }
      startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
      dismiss()
      backToMain()
    }
  }

  private fun backToMain() {
    val action = ProgressFragmentDirections.actionProgressFragmentToFilePickerFragment()
    findNavController().navigate(action)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentProgressBinding.inflate(layoutInflater)
    binding.converting.text = getString(R.string.converting, args.convertOptions.midiFile.name)
    return binding.root
  }

}