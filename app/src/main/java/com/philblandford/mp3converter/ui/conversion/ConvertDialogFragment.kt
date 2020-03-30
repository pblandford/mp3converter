package com.philblandford.mp3converter.ui.conversion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.philblandford.mp3converter.R

class ConvertDialogFragment : DialogFragment() {

  private val args: ConvertDialogFragmentArgs by navArgs()
  private val viewModel: ConversionViewModel by activityViewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.midiFileDescr = args.midiFile
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_convert_dialog, container, false)
  }

}