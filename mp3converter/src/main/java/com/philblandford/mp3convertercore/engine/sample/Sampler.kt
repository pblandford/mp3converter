package com.philblandford.mp3convertercore.engine.sample

import com.philblandford.mp3convertercore.engine.file.input.MidiEvent

// Microseconds
typealias Ms = Long
typealias Delta = Int

interface ISampler {
  fun open()
  fun close()
  fun passEvent(midiEvent: MidiEvent)
  fun getSample(length: Ms):List<Short>
}

