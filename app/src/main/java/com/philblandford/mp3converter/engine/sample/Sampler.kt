package com.philblandford.mp3converter.engine.sample

import com.philblandford.mp3converter.engine.file.input.MidiEvent

// Microseconds
typealias Ms = Long
typealias Delta = Int

interface ISampler {
  fun passEvent(midiEvent: MidiEvent)
  fun getSample(length:Ms):List<Short>
}

