package com.philblandford.mp3converter.ui.filepicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.philblandford.mp3converter.MidiFileDescr
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FilePickerBinding
import com.philblandford.mp3converter.databinding.FilePickerItemBinding


class FilePickerFragment() : Fragment() {


  private lateinit var binding: FilePickerBinding
  private val viewModel: FilePickerViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FilePickerBinding.inflate(inflater)
    initSwiper()
    initRecyclerView()
    return binding.root
  }

  private fun initSwiper() {
    binding.fileListSwiper.setOnRefreshListener {
      initRecyclerView()
      binding.fileListSwiper.isRefreshing = false
    }
  }

  private fun initRecyclerView() {
    val names = viewModel.getFileNames()
    val adapter = FileAdapter(names)
    binding.fileList.adapter = adapter
    binding.fileList.layoutManager = LinearLayoutManager(context)
  }

  private inner class FileViewHolder(view: View, getFile: (Int) -> MidiFileDescr?) :
    RecyclerView.ViewHolder(view) {
    init {
      view.setOnClickListener {
        getFile(layoutPosition)?.let { file ->
          navigateToConvertOptions(file)
        }
      }
    }
  }

  private fun navigateToConvertOptions(file: MidiFileDescr) {
    val action =
      FilePickerFragmentDirections.actionFilePickerFragmentToConvertDialogFragment(file)
    findNavController().navigate(action)
  }

  private inner class FileAdapter(val fileNames: List<MidiFileDescr>) :
    RecyclerView.Adapter<FileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
      val binding = FilePickerItemBinding.inflate(LayoutInflater.from(parent.context))
      return FileViewHolder(binding.root) { fileNames[it] }
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
      val file = fileNames[position]
      holder.itemView.findViewById<TextView>(R.id.file_text).text = file.name

    }

    override fun getItemCount(): Int {
      return fileNames.size
    }
  }
}