package com.philblandford.mp3converter.ui.conversion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.philblandford.mp3converter.*
import com.philblandford.mp3converter.databinding.FragmentConvertOptionsBinding


class ConvertOptionsFragment : Fragment() {
  private lateinit var binding: FragmentConvertOptionsBinding
  private val viewModel:ConversionViewModel by activityViewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

  }

  override fun onDestroyView() {
    activity?.title = getString(R.string.app_name)
    super.onDestroyView()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentConvertOptionsBinding.inflate(inflater)
    binding.buttonGo.setOnClickListener {
      startConversion()
    }
    viewModel.midiFileDescr?.let { midiFile ->
      activity?.title = getString(R.string.converting, midiFile.name)
    }
    return binding.root
  }

  private fun startConversion() {
    viewModel.setType(binding.buttonMp3.isChecked)
    val action = ConvertOptionsFragmentDirections.actionConvertOptionsFragmentToProgressFragment()
    findNavController().navigate(action)
  }

}