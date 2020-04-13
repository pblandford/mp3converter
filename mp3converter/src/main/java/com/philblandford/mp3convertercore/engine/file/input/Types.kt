package com.philblandford.mp3convertercore.engine.file.input

typealias Tick = Int

const val EVENT_NOTE_OFF = 0x80
const val EVENT_NOTE_ON = 0x90
const val EVENT_POLYPHONIC_PRESSURE = 0xa0
const val EVENT_CONTROLLER = 0xb0
const val EVENT_PROGRAM_CHANGE = 0xc0
const val EVENT_CHANNEL_PRESSURE = 0xd0
const val EVENT_PITCH_BEND = 0xe0

const val EVENT_META = 0xff
const val EVENT_META_TEXT = 0x01
const val EVENT_META_COPYRIGHT = 0x02
const val EVENT_META_TRACK_NAME = 0x03
const val EVENT_META_INSTRUMENT_NAME = 0x04
const val EVENT_META_LYRIC = 0x05
const val EVENT_META_MARKER = 0x06
const val EVENT_META_CUE_POINT = 0x07
const val EVENT_META_PROGRAM_NAME = 0x08
const val EVENT_META_DEVICE_NAME = 0x09
const val EVENT_META_MIDI_CHANNEL_PREFIX = 0x20
const val EVENT_META_MIDI_PORT = 0x21
const val EVENT_META_END_OF_TRACK = 0x2f
const val EVENT_META_TEMPO = 0x51
const val EVENT_META_SMPTE_OFFSET = 0x54
const val EVENT_META_SEQUENCER_SPECIFIC = 0x7f
const val EVENT_META_TIME_SIGNATURE = 0x58
const val EVENT_META_KEY_SIGNATURE = 0x59


const val EVENT_SYSEX = 0xf0


enum class MidiFormat {
  SINGLE,
  MULTI,
  SEQUENTIAL
}

enum class TimeType {
  TIMECODE,
  METRICAL
}

sealed class MidiComponent

sealed class MidiEvent(
  open val channel: Int,
  open val priority: Int
) : MidiComponent()

data class TextEvent(val text: String) : MetaEvent(5)
data class CopyrightEvent(val text:String) : MetaEvent(5)
data class TrackNameEvent(val text:String) : MetaEvent(5)
data class InstrumentNameEvent(val text:String) : MetaEvent(5)
data class LyricEvent(val text:String) : MetaEvent(5)
data class MarkerEvent(val text:String) : MetaEvent(5)
data class CuePointEvent(val text:String) : MetaEvent(5)
data class ProgramNameEvent(val text:String) : MetaEvent(5)
data class DeviceNameEvent(val text:String) : MetaEvent(5)
data class MidiChannelPrefixEvent(val prefix:Int) : MetaEvent(5)
data class MidiPortEvent(val port:Int) : MetaEvent(5)
data class SmpteOffsetEvent(val hour:Int, val minute:Int, val second:Int, val frames:Int, val frameFractions:Int) : MetaEvent(5)
data class EndTrackEvent(val dummy: Int = 0) : MetaEvent(5)
data class SequencerSpecificEvent(val data:List<Byte>) : MetaEvent(5)

data class TimeSignatureEvent(val numerator: Int, val denominator: Int) : MetaEvent(4)
data class TempoEvent(val msPerCrotchet: Long) : MetaEvent(4)
data class KeySignatureEvent(val sharps: Int) : MetaEvent(4)

sealed class MetaEvent(override val priority: Int) : MidiEvent(0, priority)

data class PedalEvent(override val channel: Int, val on: Boolean) : MidiEvent(channel, 3)
data class NoteOnEvent(val midiVal: Int, val velocity: Int, override val channel: Int) :
  MidiEvent(channel, 2)

data class NoteOffEvent(val midiVal: Int, val velocity: Int, override val channel: Int) : MidiEvent(channel, 1)
data class PolyphonicPressureEvent(val midiVal: Int, val pressure: Int, override val channel: Int) :
  MidiEvent(channel, 1)

data class ControllerEvent(val controller: Int, val value: Int, override val channel: Int) :
  MidiEvent(channel, 0)

data class ChannelPressureEvent(val pressure: Int, override val channel: Int) : MidiEvent(channel, 0)

data class PitchBendEvent(val bend: Int, override val channel: Int) : MidiEvent(channel, 0)

data class SysexEvent(val bytes:List<Byte>) : MidiEvent(0, 0)

data class ProgramChangeEvent(
  val program: Int, override val channel: Int,
  val soundFont: String, val bank: Int
) : MidiEvent(channel, 0)


data class Header(
    val format: MidiFormat,
    val tracks: Int,
    val timingInterval: Int,
    val timeType: TimeType
) : MidiComponent()

data class Track(val events: List<Pair<Int, MidiEvent>>) : MidiComponent()
data class TrackChunk(val tracks: List<Track>) : MidiComponent()

data class MidiFile(val header: Header, val trackChunk: TrackChunk) : MidiComponent()