package com.philblandford.mp3convertercore.engine.sample

import android.util.Log
import com.philblandford.mp3convertercore.engine.file.input.*


const val PERIOD_SIZE = 64

class FluidSampler(private val soundFontPath: String) :
  ISampler {

  init {
    System.loadLibrary("fluid-native")
  }

  override fun open(sampleRate: Int) {
    openFluid(sampleRate, soundFontPath)
  }

  override fun close() {
  }

  override fun passEvent(midiEvent: MidiEvent) {
    Log.d("FLD", "Passing event $midiEvent")
    val channel = midiEvent.channel
    when (midiEvent) {
        is ProgramChangeEvent -> programChange(channel, midiEvent.program)
        is NoteOnEvent -> noteOn(channel, midiEvent.midiVal, midiEvent.velocity)
        is NoteOffEvent -> noteOff(channel, midiEvent.midiVal)
        is ControllerEvent -> controlChange(channel, midiEvent.controller, midiEvent.value)
        is ChannelPressureEvent -> channelPressure(channel, midiEvent.pressure)
    }
  }

  override fun getSample(length: Ms, sampleRate: Int): List<Short> {

    if (length < 0) {
      Log.e("FLDS", "Length is $length")
      return listOf()
    }

    val numShorts = (length * sampleRate / 1000) / 1000
    val dataShorts = getSampleData(numShorts).toList()
    return dataShorts
  }

}

external fun openFluid(sampleRate: Int, soundFontPath: String)
external fun getSampleData(length: Long): ShortArray
external fun programChange(channel: Int, midiId: Int)
external fun noteOn(channel: Int, midiVal: Int, velocity: Int)
external fun noteOff(channel: Int, midiVal: Int)
external fun controlChange(channel: Int, function: Int, value: Int)
external fun channelPressure(channel: Int, pressure: Int)