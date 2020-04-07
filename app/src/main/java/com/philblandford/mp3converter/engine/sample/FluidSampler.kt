package com.philblandford.mp3converter.engine.sample

import android.util.Log
import com.philblandford.mp3converter.SAMPLE_RATE
import com.philblandford.mp3converter.engine.file.input.MidiEvent
import com.philblandford.mp3converter.engine.file.input.NoteOffEvent
import com.philblandford.mp3converter.engine.file.input.NoteOnEvent
import com.philblandford.mp3converter.engine.file.input.ProgramChangeEvent


const val PERIOD_SIZE = 64

class FluidSampler(private val soundFontPath: String) : ISampler {

  init {
    System.loadLibrary("fluid-native")
  }

  override fun open() {
    openFluid(soundFontPath)
  }

  override fun close() {
  }

  override fun passEvent(midiEvent: MidiEvent) {
    Log.d("FLD", "Passing event $midiEvent")
    val channel = midiEvent.channel
    when (midiEvent) {
      is ProgramChangeEvent -> {
        programChange(channel, midiEvent.program)
      }
      is NoteOnEvent -> noteOn(channel, midiEvent.midiVal, midiEvent.velocity)
      is NoteOffEvent -> noteOff(channel, midiEvent.midiVal)
    }
  }

  override fun getSample(length: Ms): List<Short> {

    if (length < 0) {
      Log.e("FLDS", "Length is $length")
      return listOf()
    }

    val numShorts = (length * SAMPLE_RATE/1000) / 1000
    val dataShorts = getSampleData(numShorts).toList()
    return dataShorts
  }

}

external fun openFluid(soundFontPath: String)
external fun getSampleData(length: Long): ShortArray
external fun programChange(channel: Int, midiId: Int)
external fun noteOn(channel: Int, midiVal: Int, velocity: Int)
external fun noteOff(channel: Int, midiVal: Int)