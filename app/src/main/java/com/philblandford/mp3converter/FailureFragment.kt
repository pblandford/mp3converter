package com.philblandford.mp3converter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.philblandford.mp3converter.databinding.FragmentFailureBinding
import com.philblandford.mp3converter.ui.conversion.ConversionViewModel


class FailureFragment : Fragment() {

  private val viewModel: ConversionViewModel by activityViewModels()
  private lateinit var binding: FragmentFailureBinding

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentFailureBinding.inflate(inflater)
    val message = viewModel.failMessage?.let { message ->
      getString(R.string.convert_failed, viewModel.midiFileDescr?.name ?: "", message)
    } ?: getString(R.string.failure_unknown)
    binding.textFailure.text = message
    return binding.root
  }

}