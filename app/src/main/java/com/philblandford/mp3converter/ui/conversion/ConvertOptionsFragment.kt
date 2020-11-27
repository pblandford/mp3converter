package com.philblandford.mp3converter.ui.conversion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.Slider
import com.philblandford.mp3converter.*
import com.philblandford.mp3converter.databinding.FragmentConvertOptionsBinding
import com.philblandford.mp3convertercore.api.ExportType
import com.philblandford.mp3convertercore.engine.CD_SAMPLE_RATE
import com.philblandford.mp3convertercore.engine.CUSTOM_SAMPLE_RATE
import com.philblandford.mp3convertercore.engine.Settings


class ConvertOptionsFragment : Fragment() {
  private lateinit var binding: FragmentConvertOptionsBinding
  private val viewModel: ConversionViewModel by activityViewModels()

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

    viewModel.getSettings().value?.let { settings ->
      viewModel.getExportType().value?.let { exportType ->

        initChangeable(settings)

        viewModel.getExportType().observe(viewLifecycleOwner) {
          initChangeable(settings)
        }
        viewModel.getSettings().observe(viewLifecycleOwner) {
          initChangeable(it)
        }

        binding.formatButtonGroup.check(
          if (exportType == ExportType.MP3) R.id.button_mp3 else R.id.button_wav)

        initSliders(settings)

        binding.cdCheck.isChecked = settings.sampleRate == CD_SAMPLE_RATE
        binding.cdCheck.setOnCheckedChangeListener { buttonView, isChecked ->
          viewModel.setSampleRate(if (isChecked) CD_SAMPLE_RATE else CUSTOM_SAMPLE_RATE)
        }
        binding.formatButtonGroup.setOnCheckedChangeListener { group, checkedId ->
          viewModel.setType(checkedId == R.id.button_mp3)
        }
        viewModel.midiFileDescr?.let { midiFile ->
          activity?.title = getString(R.string.converting, midiFile.name)
        }
      }
    }
    return binding.root
  }

  private fun initChangeable(settings: Settings) {
    setVisibilities()
    setBitText()
    setSampleSlider(settings)
  }

  private fun setVisibilities() {
    val exportType = viewModel.getExportType().value
    if (exportType == ExportType.MP3) {
      binding.bitrateSlider.visibility = View.VISIBLE
    } else {
      binding.bitrateSlider.visibility = View.GONE
    }
    binding.sampleSlider.isEnabled = viewModel.getSettings().value?.sampleRate == CD_SAMPLE_RATE
  }

  private fun setBitText() {
    val exportType = viewModel.getExportType().value
    if (exportType == ExportType.MP3) {
      binding.bitText.visibility = View.VISIBLE
      binding.bitText.text = getString(R.string.bitrate, viewModel.getSettings().value?.bitRate
        ?: 128)
    } else {
      binding.bitText.visibility = View.GONE
    }
    binding.sampleText.text = getString(R.string.samplerate,
      (viewModel.getSettings().value?.sampleRate ?: 48000) / 1000)
  }

  private fun setSampleSlider(settings: Settings) {
    val slider = binding.sampleSlider
    val yes = settings.sampleRate != CD_SAMPLE_RATE
    slider.isEnabled = yes
    val max = viewModel.sampleValues.end
    slider.valueTo = max.toFloat()
    if (slider.isEnabled && settings.sampleRate != slider.value.toInt()) {
      slider.value = settings.sampleRate.toFloat()
    } else if (!slider.isEnabled) {
      slider.value = CUSTOM_SAMPLE_RATE.toFloat()
    }
  }

  private fun initSliders(settings: Settings) {
    binding.bitrateSlider.init(viewModel.bitRateValues, settings.bitRate) {
      viewModel.setBitRate(it)
    }
    binding.sampleSlider.init(viewModel.sampleValues, settings.sampleRate) {
      viewModel.setSampleRate(it)
    }
  }

  private fun Slider.init(range: SliderRange, init: Int, onChange: (Int) -> Unit) {
    valueFrom = range.start.toFloat()
    valueTo = range.end.toFloat()

    value = ((init / range.step) * range.step).toFloat()
    stepSize = range.step.toFloat()
    addOnChangeListener { slider, value, fromUser ->
      if (isEnabled) {
        onChange(value.toInt())
      }
    }
  }

  private fun startConversion() {
    val action = ConvertOptionsFragmentDirections.actionConvertOptionsFragmentToProgressFragment()
    findNavController().navigate(action)
  }

}