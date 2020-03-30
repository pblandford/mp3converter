package com.philblandford.mp3converter

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.philblandford.mp3converter.databinding.FragmentSuccessBinding
import org.apache.commons.io.FilenameUtils


class SuccessFragment : DialogFragment() {

  private lateinit var binding: FragmentSuccessBinding
  private val args: SuccessFragmentArgs by navArgs()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val builder = AlertDialog.Builder(context)
    builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
      backToMain()
    }
    val name = FilenameUtils.getBaseName(args.path)
    binding = FragmentSuccessBinding.inflate(layoutInflater)
    binding.textSuccess.text = getString(R.string.convert_success, name, args.path)
    builder.setView(binding.root)
    return builder.create()
  }

  private fun backToMain() {
    val action = SuccessFragmentDirections.actionSuccessFragmentToFilePickerFragment()
    findNavController().navigate(action)
  }
}