package com.philblandford.mp3converter.ui.options

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.philblandford.mp3converter.ConvertOptions
import com.philblandford.mp3converter.DestinationType
import com.philblandford.mp3converter.ExportType
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FragmentConvertOptionsBinding
import org.koin.android.ext.android.bind


class ConvertOptionsFragment : Fragment() {
  private val args: ConvertOptionsFragmentArgs by navArgs()
  private lateinit var binding: FragmentConvertOptionsBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activity?.title = getString(R.string.converting, args.midiFile.name)
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
    return binding.root
  }

  private fun startConversion() {
    val type = if (binding.buttonMp3.isChecked) ExportType.MP3 else ExportType.WAV
    val destination =
      if (binding.buttonLocal.isChecked) DestinationType.LOCAL else DestinationType.SHARE
    val action = ConvertOptionsFragmentDirections.actionConvertOptionsFragmentToProgressFragment(
      ConvertOptions(type, destination, args.midiFile)
    )
    findNavController().navigate(action)
  }

}