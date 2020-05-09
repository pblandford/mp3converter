package com.philblandford.mp3converter.ui.filepicker

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.philblandford.mp3convertercore.MediaFileDescr
import com.philblandford.mp3converter.R
import com.philblandford.mp3converter.databinding.FilePickerBinding
import com.philblandford.mp3converter.databinding.FilePickerItemBinding
import com.philblandford.mp3converter.ui.conversion.ConversionViewModel
import com.philblandford.mp3converter.ui.conversion.ConvertStatus
import com.philblandford.mp3converter.ui.conversion.Status


class FilePickerFragment() : Fragment() {

  private lateinit var binding: FilePickerBinding
  private val viewModel: FilePickerViewModel by viewModels()
  private val conversionViewModel: ConversionViewModel by activityViewModels()
  private lateinit var files: List<MediaFileDescr>

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    files = viewModel.getConvertedFilesStatic()
    binding = FilePickerBinding.inflate(inflater)
    initPickButton()
    return binding.root
  }

  override fun onResume() {
    super.onResume()
    initRecyclerView()
    refreshRecyclerView()
    conversionViewModel.getStatus().observe(viewLifecycleOwner, Observer {
      if (it.status == Status.COMPLETED) {
        refreshRecyclerView()
      }
    })
  }

  private fun openDocTree() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "*/*"
      putExtra(
        Intent.EXTRA_MIME_TYPES, arrayOf(
        "audio/midi", "audio/x-midi", "application/x-midi",
        "audio/x-mid"
      )
      )
    }
    startActivityForResult(intent, 0)
  }

  private fun initPickButton() {
    binding.buttonSelect.setOnClickListener {
      openDocTree()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(
      requestCode,
      resultCode,
      data
    ); if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

      Log.i("TAG", "${data?.data}")
      data?.data?.let { uri ->
        viewModel.getMidiDescr(uri)?.let {
          navigateToConvertOptions(it)
        }
      }
    }
  }


  private fun initRecyclerView() {

    val manager = LinearLayoutManager(context)
    binding.recyclerConverted.layoutManager = manager

    val dividerItemDecoration = DividerItemDecoration(context, manager.orientation)
    binding.recyclerConverted.addItemDecoration(dividerItemDecoration)

    if (binding.recyclerConverted.adapter?.itemCount == 0) {
      binding.textConverted.visibility = View.GONE
    } else {
      binding.textConverted.visibility = View.VISIBLE
    }
  }

  private fun refreshRecyclerView() {
    files = viewModel.getConvertedFilesStatic()
    val adapter = FileAdapter(files)
    binding.recyclerConverted.adapter = adapter
  }

  private inner class FileViewHolder(view: View, getDescr: (Int) -> MediaFileDescr?) :
    RecyclerView.ViewHolder(view) {
    init {
      view.setOnClickListener {
        getDescr(layoutPosition)?.let { descr ->
          navigateToPlay(descr)
        }
      }
    }
  }

  private fun navigateToConvertOptions(midiFileDescr: MediaFileDescr) {
    val action =
      FilePickerFragmentDirections.actionFilePickerFragmentToConvertDialogFragment(
        midiFileDescr
      )
    findNavController().navigate(action)
  }

  private fun navigateToPlay(descr: MediaFileDescr) {
    val action = FilePickerFragmentDirections.actionFilePickerFragmentToPlayFragment(descr)
    findNavController().navigate(action)
  }

  private inner class FileAdapter(val fileDescrs: List<MediaFileDescr>) :
    RecyclerView.Adapter<FileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
      val binding =
        FilePickerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
      return FileViewHolder(binding.root) { fileDescrs[it] }
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
      val descr = fileDescrs[position]
      holder.itemView.findViewById<TextView>(R.id.file_text).text = descr.name
      holder.itemView.findViewById<View>(R.id.delete_button).initDelete(descr)
    }

    override fun getItemCount(): Int {
      return fileDescrs.size
    }
  }

  private fun View.initDelete(descr: MediaFileDescr) {
    setOnClickListener {
      AlertDialog.Builder(context)
        .setMessage(getString(R.string.delete_confirm, descr.name))
        .setPositiveButton(R.string.ok) { _, _ ->
          descr.uri.toFile().delete()
          refreshRecyclerView()
        }.setNegativeButton(R.string.cancel) { dialog, _ ->
          dialog.dismiss()
        }
        .show()
    }
  }
}