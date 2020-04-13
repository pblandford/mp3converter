package com.philblandford.mp3convertercore.engine.file.input

import com.philblandford.mp3convertercore.engine.file.input.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test

import org.junit.Assert.*

class MIdiFileReaderKtTest {

  @Test
  fun readHeader() {
    val hex = "000000060001000101e0"
    val bytes = "MThd".toByteArray() + getBytes(hex)
    val br =
        ByteReader(bytes)
    val either = br.readHeader()
    assertThat(
      either as Right<Header>,
      `is`(
          Right(
              Header(
                  MidiFormat.MULTI,
                  1,
                  480,
                  TimeType.METRICAL
              )
          )
      )
    )
  }

  @Test
  fun testReadDeltaTime0() {
    val br = ByteReader(
        getBytes("00")
    )
    val either = br.readDeltaTime()
    assertThat(either as Right<Int>, `is`(
        Right(0)
    ))
  }

  @Test
  fun testReadDeltaTime0x40() {
    val br = ByteReader(
        getBytes("40")
    )
    val either = br.readDeltaTime()
    assertThat(either as Right<Int>, `is`(
        Right(0x40)
    ))
  }

  @Test
  fun testReadDeltaTimeTwoBytes() {
    val br = ByteReader(
        getBytes("8100")
    )
    val either = br.readDeltaTime()
    assertThat(either as Right<Int>, `is`(
        Right(0x80)
    ))
  }

  @Test
  fun testReadDeltaTimeTwoBytesac6e() {
    val br = ByteReader(
        getBytes("ac6e")
    )
    val either = br.readDeltaTime()
    assertThat(either as Right<Int>, `is`(
        Right(5742)
    ))
  }


  @Test
  fun testReadDeltaTimeThreeBytes() {
    val br = ByteReader(
        getBytes("c08000")
    )
    val either = br.readDeltaTime()
    assertThat(either as Right<Int>, `is`(
        Right(0x100000)
    ))
  }

  @Test
  fun testReadNoteOff() {
    val br = ByteReader(
        getBytes("804040")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0x80.toUByte(),
                  NoteOffEvent(
                      64,
                      64,
                      0
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadNoteOn() {
    val br = ByteReader(
        getBytes("90407f")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0x90.toUByte(),
                  NoteOnEvent(
                      64,
                      127,
                      0
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadNoteOnChannel1() {
    val br = ByteReader(
        getBytes("91407f")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0x91.toUByte(),
                  NoteOnEvent(
                      64,
                      127,
                      1
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadPolyphonicPressureEvent() {
    val br = ByteReader(
        getBytes("a04050")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xa0.toUByte(),
                  PolyphonicPressureEvent(
                      64,
                      80,
                      0
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadControllerChangeEvent() {
    val br = ByteReader(
        getBytes("b00140")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xb0.toUByte(),
                  ControllerEvent(
                      1,
                      64,
                      0
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadProgramChange() {
    val br = ByteReader(
        getBytes("c040")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xc0.toUByte(),
                  ProgramChangeEvent(
                      64,
                      0,
                      "",
                      0
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadChannelPressureEvent() {
    val br = ByteReader(
        getBytes("d040")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xd0.toUByte(),
                  ChannelPressureEvent(
                      64,
                      0
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadPitchBendEvent() {
    val br = ByteReader(
        getBytes("e04040")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xe0.toUByte(),
                  PitchBendEvent(
                      0x2040,
                      0
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadTextEvent() {
    val br = ByteReader(
        getBytes("ff0104").plus("TWAT".toByteArray())
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  TextEvent(
                      "TWAT"
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadCopyrightEvent() {
    val br = ByteReader(
        getBytes("ff0204").plus("TWAT".toByteArray())
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  CopyrightEvent(
                      "TWAT"
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadTrackNameEvent() {
    val br = ByteReader(
        getBytes("ff0304").plus("TWAT".toByteArray())
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  TrackNameEvent(
                      "TWAT"
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadMidiPortEvent() {
    val br = ByteReader(
        getBytes("ff210104")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  MidiPortEvent(
                      4
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadEndOfTrack() {
    val br = ByteReader(
        getBytes("ff2f00")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  EndTrackEvent()
              )
          )
      )
    )
  }

  @Test
  fun testReadSysexEvent() {
    val br = ByteReader(
        getBytes("f00500010203f7")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(Right(
          ReadEventReturn(
              0xf0.toUByte(),
              SysexEvent(
                  (0..3).map { it.toByte() }
              ))))
    )
  }

  @Test
  fun testReadTempoEvent() {
    val br = ByteReader(
        getBytes("ff5103abcdef")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  TempoEvent(
                      0xabcdef
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadTimeSignatureEvent() {
    val br = ByteReader(
        getBytes("ff580404020008")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  TimeSignatureEvent(
                      4,
                      4
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadTimeSignatureEvent3_16() {
    val br = ByteReader(
        getBytes("ff580403040008")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  TimeSignatureEvent(
                      3,
                      16
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadKeySignatureEvent() {
    val br = ByteReader(
        getBytes("ff59020400")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  KeySignatureEvent(
                      4
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadSMPTEOffsetEvent() {
    val br = ByteReader(
        getBytes("ff54050102030405")
    )
    val either = br.readEvent()
    assertThat(
      either as Right<ReadEventReturn>,
      `is`(
          Right(
              ReadEventReturn(
                  0xff.toUByte(),
                  SmpteOffsetEvent(
                      1,
                      2,
                      3,
                      4,
                      5
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadDeltaEventPair() {
    val br = ByteReader(
        getBytes("0090407f")
    )
    val either = br.readDeltaEvent()
    assertThat(
      either as Right<DeltaEventReturn>,
      `is`(
          Right(
              DeltaEventReturn(
                  0x90.toUByte(),
                  0,
                  NoteOnEvent(
                      64,
                      127,
                      0
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadEmptyTrack() {
    val hex = "0000000400ff2f00"
    val bytes = "MTrk".toByteArray() + getBytes(hex)
    val br =
        ByteReader(bytes)
    val either = br.readTrack()
    assertEquals(listOf(0 to EndTrackEvent()), (either as Right<Track>).r.events.toList())
  }

  @Test
  fun testReadTrackOneEvent() {
    val hex = "000000080090407f00ff2f00"
    val bytes = "MTrk".toByteArray() + getBytes(hex)
    val br =
        ByteReader(bytes)
    val either = br.readTrack()
    assertThat(
      either as Right<Track>,
      `is`(
          Right(
              Track(
                  listOf(
                      0 to NoteOnEvent(
                          64,
                          127,
                          0
                      ),
                      0 to EndTrackEvent()
                  )
              )
          )
      )
    )
  }

  @Test
  fun testReadTrackRunningStatus() {
    val hex = "0000000b0090407f20417f00ff2f00"
    val bytes = "MTrk".toByteArray() + getBytes(hex)
    val br =
        ByteReader(bytes)
    val either = br.readTrack()
    assertThat(
      either as Right<Track>,
      `is`(
          Right(
              Track(
                  listOf(
                      0 to NoteOnEvent(
                          64,
                          127,
                          0
                      ),
                      32 to NoteOnEvent(
                          65,
                          127,
                          0
                      ),
                      0 to EndTrackEvent()
                  )
              )
          )
      )
    )
  }


  private fun getBytes(hexString: String): ByteArray {
    val bytes = hexString.chunked(2).map { byteStr ->
      ((byteStr[0].nibble().toUInt() shl 4) or (byteStr[1].nibble().toUInt())).toByte()
    }
    return bytes.toByteArray()
  }

  private val hexChars = "0123456789abcdef".withIndex().map { it.value to it.index }.toMap()
  private fun Char.nibble(): Byte {
    return hexChars[this]?.toByte() ?: 0
  }
}