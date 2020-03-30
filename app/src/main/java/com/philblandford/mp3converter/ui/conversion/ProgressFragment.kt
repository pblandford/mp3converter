package com.philblandford.mp3converter.ui.conversion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.philblandford.mp3converter.ExportType
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FragmentProgressBinding


class ProgressFragment : Fragment() {

  private lateinit var binding: FragmentProgressBinding
  private val viewModel: ConversionViewModel by activityViewModels()

  override fun onResume() {
    super.onResume()
    viewModel.convertFile(
      { binding.progressBar.setProgress(it, false) }, ::complete
    ) {
      activity?.runOnUiThread {
        Toast.makeText(
          context,
          getString(
            R.string.convert_failed,
            viewModel.midiFileDescr?.name ?: "",
            it.message
          ),
          Toast.LENGTH_LONG
        ).show()
      }
    }
  }

  private fun complete() {
    showSuccess()
  }

  private fun showSuccess() {
    val path = viewModel.path ?: "Unknown"
    val action = ProgressFragmentDirections.actionProgressFragmentToBlankFragment()
    findNavController().navigate(action)
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
    binding.converting.text = getString(R.string.converting, viewModel.midiFileDescr?.name ?: "")
    return binding.root
  }

}