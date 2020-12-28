package com.philblandford.mp3convertercore.engine.file.input

import java.lang.Math.pow

data class Failure(val byte: Int, val message: String)

sealed class Either<out L, out R>

data class Left<out L>(val l: L) : Either<L, Nothing>()
data class Right<out R>(val r: R) : Either<Nothing, R>()

private inline fun <L, R1, R2> Either<L, R1>.flatMap(f: (R1) -> Either<L, R2>): Either<L, R2> =
    when (this) {
      is Left -> this
      is Right -> f(this.r)
    }


fun readMidiFile(bytes: List<Byte>): Either<Failure, MidiFile> {
  val byteReader =
      ByteReader(bytes.toByteArray())
  return byteReader.readHeader().flatMap { header ->
    byteReader.readTracks(header.tracks).flatMap { trackChunk ->
      Right(
          MidiFile(
              header,
              trackChunk
          )
      )
    }
  }
}

internal fun ByteReader.readHeader(): Either<Failure, Header> {
  val chunkID = getNextString(4)
  if (chunkID != "MThd") {
    return Left(
        Failure(
            getIdx() - 4,
            "Not a valid MIDI file"
        )
    )
  }
  val length = getNextInt() // FIXME - check length
  val formatInt = getNextShort().toInt()
  val format = getFormat(
      formatInt
  ) ?: run {
    return Left(
        Failure(
            getIdxLastShort(),
            "Unknown format"
        )
    )
  }
  val numTracks = getNextShort().toInt()
  val tickDescr = getNextByte()
  val timeType =
      getTimeType(
          tickDescr
      )
  val ppqn = ((tickDescr.toUInt() shl 8) or getNextByte().toUInt()).toInt()
  val header = Header(
      format,
      numTracks,
      ppqn,
      timeType
  )

  return Right(header)
}

internal fun ByteReader.readTracks(num: Int): Either<Failure, TrackChunk> {

  val tracks = (1..num).map { n ->
    when (val either = readTrack()) {
      is Left -> return either
      is Right -> either
    }
  }.map { it.r }
  return Right(
      TrackChunk(tracks)
  )
}

internal fun ByteReader.readTrack(): Either<Failure, Track> {
  val chunkId = getNextString(4)
  if (chunkId != "MTrk" && chunkId.take(3) != "SEM") {
    return Left(
        Failure(
            getIdx(),
            "Expected MTrk ID, got $chunkId"
        )
    )
  }
  val length = getNextInt().toInt()
  val start = getIdx()
  val events = mutableListOf<Pair<Tick, MidiEvent>>()

  var lastTypeChan: UByte? = null
  while ((getIdx() - start) < length) {
    when (val res = readDeltaEvent(lastTypeChan)) {
      is Left -> return Left(
          res.l
      )
      is Right -> {
        events.add(res.r.tick to res.r.midiEvent)
        lastTypeChan = res.r.lastTypeChan
      }
    }
  }

  return if (events.last().second !is EndTrackEvent) {
    Left(
        Failure(
            getIdx(),
            "No end of track event found"
        )
    )
  } else {
    Right(
        Track(events)
    )
  }
}

internal data class DeltaEventReturn(
    val lastTypeChan: UByte,
    val tick: Tick,
    val midiEvent: MidiEvent
)

internal fun ByteReader.readDeltaEvent(lastTypeChan: UByte? = null): Either<Failure, DeltaEventReturn> {
  return readDeltaTime().flatMap { dt ->
    readEvent(lastTypeChan).flatMap { event ->
      Right(
          DeltaEventReturn(
              event.typeChan,
              dt,
              event.midiEvent
          )
      )
    }
  }
}

internal fun ByteReader.readDeltaTime(): Either<Failure, Int> {
  val bytes = mutableListOf<UByte>()
  do {
    val next = getNextByte()
    bytes.add(next)
    if (next and 0x80.toUByte() == 0.toUByte()) {
      break
    }
  } while (true)
  val result = bytes.reversed().withIndex().fold(0.toUInt()) { res, iv ->
    res or ((iv.value.toUInt() and 0x7f.toUInt()) shl (iv.index * 7))
  }
  return Right(result.toInt())
}

data class ReadEventReturn(val typeChan: UByte, val midiEvent: MidiEvent)

internal fun ByteReader.readEvent(lastTypeChan: UByte? = null): Either<Failure, ReadEventReturn> {
  val nextByte = peekNextByte()
  val running = lastTypeChan != null && nextByte and 0x80.toUByte() == 0.toUByte()
  val typeChan = if (running) lastTypeChan!!.toUByte() else getNextByte()
  val type = if (typeChan and 0xf0.toUByte() == 0xf0.toUByte()) {
    typeChan
  } else {
    typeChan and 0xf0.toUByte()
  }
  val channel = (typeChan and 0x0f.toUByte()).toInt()
  val either = when (type.toInt()) {
    EVENT_NOTE_OFF -> readNoteOff(channel)
    EVENT_NOTE_ON -> readNoteOn(channel)
    EVENT_POLYPHONIC_PRESSURE -> readPolyphonicPressure(channel)
    EVENT_CONTROLLER -> readController(channel)
    EVENT_PROGRAM_CHANGE -> readProgramChange(channel)
    EVENT_CHANNEL_PRESSURE -> readChannelPressure(channel)
    EVENT_PITCH_BEND -> readPitchBend(channel)
    EVENT_META -> readMetaEvent()
    EVENT_SYSEX -> readSysexEvent()
    else -> Left(
        Failure(
            getIdxLastByte(),
            "Unknown event ${type}"
        )
    )
  }
  return when (either) {
    is Left -> either
    is Right -> Right(
        ReadEventReturn(
            typeChan,
            either.r
        )
    )
  }
}

private fun ByteReader.readNoteOn(channel: Int): Either<Failure, NoteOnEvent> {
  val midiVal = getNextByte().toInt()
  val velocity = getNextByte().toInt()
  return Right(
      NoteOnEvent(
          midiVal,
          velocity,
          channel
      )
  )
}

private fun ByteReader.readNoteOff(channel: Int): Either<Failure, NoteOffEvent> {
  val midiVal = getNextByte().toInt()
  val velocity = getNextByte().toInt()
  return Right(
      NoteOffEvent(
          midiVal,
          velocity,
          channel
      )
  )
}

private fun ByteReader.readPolyphonicPressure(channel: Int): Either<Failure, PolyphonicPressureEvent> {
  val midiVal = getNextByte().toInt()
  val pressure = getNextByte().toInt()
  return Right(
      PolyphonicPressureEvent(
          midiVal,
          pressure,
          channel
      )
  )
}

private fun ByteReader.readController(channel: Int): Either<Failure, ControllerEvent> {
  val controller = getNextByte().toInt()
  val value = getNextByte().toInt()
  return Right(
      ControllerEvent(
          controller,
          value,
          channel
      )
  )
}

private fun ByteReader.readProgramChange(channel: Int): Either<Failure, ProgramChangeEvent> {
  val program = getNextByte().toInt()
  return Right(
      ProgramChangeEvent(
          program,
          channel,
          "",
          0
      )
  )
}

private fun ByteReader.readChannelPressure(channel: Int): Either<Failure, ChannelPressureEvent> {
  val pressure = getNextByte().toInt()
  return Right(
      ChannelPressureEvent(
          pressure,
          channel
      )
  )
}

private fun ByteReader.readPitchBend(channel: Int): Either<Failure, PitchBendEvent> {
  val lsb = getNextByte().toUInt()
  val msb = getNextByte().toUInt()
  val bend = (lsb or (msb shl 7))
  return Right(
      PitchBendEvent(
          bend.toInt(),
          channel
      )
  )
}

private fun ByteReader.readSysexEvent(): Either<Failure, SysexEvent> {
  val length = getNextByte().toInt() - 1
  val bytes = getNextString(length).toByteArray().toList()
  val term = getNextByte()
  return if (term != 0xf7.toUByte()) {
    Left(
        Failure(
            getIdxLastByte(),
            "Malformed SysEx message"
        )
    )
  } else {
    return Right(
        SysexEvent(bytes)
    )
  }
}

private fun ByteReader.readMetaEvent(): Either<Failure, MetaEvent> {
  return when (val type = getNextByte().toInt()) {
    EVENT_META_SEQUENCE -> readSequenceEvent()
    EVENT_META_TEXT -> readTextEvent<TextEvent>()
    EVENT_META_COPYRIGHT -> readTextEvent<CopyrightEvent>()
    EVENT_META_TRACK_NAME -> readTextEvent<TrackNameEvent>()
    EVENT_META_INSTRUMENT_NAME -> readTextEvent<InstrumentNameEvent>()
    EVENT_META_LYRIC -> readTextEvent<LyricEvent>()
    EVENT_META_MARKER -> readTextEvent<MarkerEvent>()
    EVENT_META_CUE_POINT -> readTextEvent<CuePointEvent>()
    EVENT_META_PROGRAM_NAME -> readTextEvent<ProgramNameEvent>()
    EVENT_META_DEVICE_NAME -> readTextEvent<DeviceNameEvent>()
    EVENT_META_MIDI_CHANNEL_PREFIX -> readByteEvent<MidiChannelPrefixEvent>()
    EVENT_META_MIDI_PORT -> readByteEvent<MidiPortEvent>()
    EVENT_META_END_OF_TRACK -> readEndOfTrack()
    EVENT_META_TEMPO -> readTempo()
    EVENT_META_TIME_SIGNATURE -> readTimeSignature()
    EVENT_META_KEY_SIGNATURE -> readKeySignature()
    EVENT_META_SMPTE_OFFSET -> readSmpteOffset()
    EVENT_META_SEQUENCER_SPECIFIC -> readSequencerSpecific()
    else -> Left(
        Failure(
            getIdxLastByte(),
            "Unknown meta event $type"
        )
    )
  }
}

private inline fun ByteReader.readSequenceEvent(): Either<Failure, MetaEvent> {
  val length = getNextByte().toInt()
  val num = getNextShort().toInt()
  return Right(SequenceNumberEvent(num))
}

private inline fun <reified T : MetaEvent> ByteReader.readTextEvent(): Either<Failure, MetaEvent> {
  val pos = getIdx()
  val length = getNextByte().toInt()
  val text = getNextString(length)
  return T::class.constructors.find { it.parameters.size == 1 }?.let { constructor ->
    Right(constructor.call(text))
  } ?: Left(Failure(pos, "Could not create event ${T::class}")
  )
}

private inline fun <reified T : MetaEvent> ByteReader.readByteEvent(): Either<Failure, MetaEvent> {
  val pos = getIdx()
  val length = getNextByte().toInt()
  val byte = getNextByte().toInt()
  return T::class.constructors.find { it.parameters.size == 1 }?.let { constructor ->
    Right(constructor.call(byte))
  } ?: Left(Failure(pos, "Could not create event ${T::class}")
  )
}

private fun ByteReader.readTempo(): Either<Failure, TempoEvent> {
  return when (getNextByte().toInt()) {
    3 -> {
      val ms = getNext24Bit()
      Right(
          TempoEvent(ms.toLong())
      )
    }
    else -> Left(
        Failure(
            getIdxLastByte(),
            "Invalid Length of Tempo event"
        )
    )
  }
}

private fun ByteReader.readTimeSignature(): Either<Failure, TimeSignatureEvent> {
  return when (getNextByte().toInt()) {
    4 -> {
      val numerator = getNextByte().toInt()
      val denominatorPower = getNextByte().toDouble()
      val denominator = pow(2.toDouble(), denominatorPower).toInt()
      getNextShort() // don't care
      return Right(
          TimeSignatureEvent(
              numerator,
              denominator
          )
      )
    }
    else -> Left(
        Failure(
            getIdxLastByte(),
            "Invalid Length of Time Signature event"
        )
    )
  }
}

private fun ByteReader.readKeySignature(): Either<Failure, KeySignatureEvent> {
  return when (getNextByte().toInt()) {
    2 -> {
      val sharps = getNextByte().toInt()
      getNextByte() // don't care
      return Right(
          KeySignatureEvent(
              sharps
          )
      )
    }
    else -> Left(
        Failure(
            getIdxLastByte(),
            "Invalid Length of Key Signature event"
        )
    )
  }
}

private fun ByteReader.readSmpteOffset(): Either<Failure, SmpteOffsetEvent> {
  return when (getNextByte().toInt()) {
    5 -> {
      val hour = getNextByte().toInt()
      val minute = getNextByte().toInt()
      val second = getNextByte().toInt()
      val frames = getNextByte().toInt()
      val frameFractions = getNextByte().toInt()
      return Right(
          SmpteOffsetEvent(
              hour,
              minute,
              second,
              frames,
              frameFractions
          )
      )
    }
    else -> Left(
        Failure(
            getIdxLastByte(),
            "Invalid Length of SMPTE offset event"
        )
    )
  }
}

private fun ByteReader.readSequencerSpecific(): Either<Failure, SequencerSpecificEvent> {
  val length = getNextByte().toInt()
  val data = getNextString(length)
  return Right(
      SequencerSpecificEvent(
          data.toByteArray().toList()
      )
  )
}

private fun ByteReader.readEndOfTrack(): Either<Failure, EndTrackEvent> {
  return when (getNextByte().toInt()) {
    0 -> Right(
        EndTrackEvent()
    )
    else -> Left(
        Failure(
            getIdxLastByte(),
            "Invalid Length of EOT event"
        )
    )
  }
}

private fun getFormat(intVal: Int): MidiFormat? {
  return MidiFormat.values().getOrNull(intVal)
}

private fun getTimeType(num: UByte): TimeType {
  return if ((num and 0x80.toUByte()) == 0.toUByte()) {
    TimeType.METRICAL
  } else {
    TimeType.TIMECODE
  }
}

private fun getPPQN(num: UShort): Int {
  return (num and 0xefff.toUShort()).toInt()
}