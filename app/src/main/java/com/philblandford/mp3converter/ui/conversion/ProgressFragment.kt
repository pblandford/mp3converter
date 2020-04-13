package com.philblandford.mp3converter.ui.conversion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FragmentProgressBinding


class ProgressFragment : Fragment() {

  private lateinit var binding: FragmentProgressBinding
  private val viewModel: ConversionViewModel by activityViewModels()

  override fun onResume() {
    super.onResume()
    bindToStatus()
    viewModel.convertFile()
  }

  private fun bindToStatus() {
    viewModel.getProgressData().observe(viewLifecycleOwner, Observer {
      it.exception?.let {
        showFailure()
      } ?: run {
        when (it.status) {
          Status.COMPLETED -> showSuccess()
          Status.IN_PROGRESS -> showProgress(it.progress)
          else -> {}
        }
      }
    })
  }

  private fun showSuccess() {
    val action = ProgressFragmentDirections.actionProgressFragmentToBlankFragment()
    findNavController().navigate(action)
  }

  private fun showFailure() {
    val action = ProgressFragmentDirections.actionProgressFragmentToFailureFragment()
    findNavController().navigate(action)
  }

  private fun showProgress(progress:Int) {
    binding.progressBar.progress = progress
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