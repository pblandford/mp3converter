package com.philblandford.mp3convertercore.engine.file.input

import com.philblandford.mp3convertercore.engine.file.input.*
import com.philblandford.mp3convertercore.engine.sample.Delta
import com.philblandford.mp3convertercore.engine.sample.ISampler
import com.philblandford.mp3convertercore.engine.sample.Ms
import junit.framework.Assert.assertEquals
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class MidiFileProcessorTest {

  @Test
  fun testDeltaToMs() {
    val ppqn = 96
    val dt = 96
    val mspc = 1000000L
    val res = deltaToMS(
        dt,
        ppqn,
        mspc
    )
    assertThat(res, `is`(1000000L))
  }

  @Test
  fun testDeltaToMs2Seconds() {
    val ppqn = 96
    val dt = 192
    val mspc = 1000L
    val res = deltaToMS(
        dt,
        ppqn,
        mspc
    )
    assertThat(res, `is`(2000L))
  }

  @Test
  fun testDeltaToMs120bpm() {
    val ppqn = 96
    val dt = 96
    val mspc = 500L
    val res = deltaToMS(
        dt,
        ppqn,
        mspc
    )
    assertThat(res, `is`(500L))
  }

  @Test
  fun testProcessEventList() {
    val events = listOf(0 to NoteOnEvent(
        42,
        100,
        0
    ),
      96 to NoteOffEvent(
          42,
          100,
          0
      )
    )
    val ppqn = 96
    fun getTempo(dt: Delta) = 1000L
    val trackChunk =
        TrackChunk(
            listOf(
                Track(
                    events
                )
            )
        )
    val processed =
        processEventList(
            trackChunk,
            MidiFormat.SINGLE,
            ppqn
        )
    assertEquals(
      listOf(
          EventSet(
              1000,
              listOf(
                  NoteOnEvent(
                      42,
                      100,
                      0
                  )
              )
          ),
          EventSet(
              0,
              listOf(
                  NoteOffEvent(
                      42,
                      100,
                      0
                  )
              )
          )
      ),
      processed
    )
  }

  @Test
  fun testProcessEventListTwoTracks() {
    val events = listOf(0 to NoteOnEvent(
        42,
        100,
        0
    ),
      96 to NoteOffEvent(
          42,
          100,
          0
      )
    )
    val events2 = listOf(0 to NoteOnEvent(
        42,
        100,
        1
    ),
      96 to NoteOffEvent(
          42,
          100,
          1
      )
    )
    val ppqn = 96
    fun getTempo(dt: Delta) = 500000L
    val trackChunk =
        TrackChunk(
            listOf(
                Track(
                    events
                ),
                Track(
                    events2
                )
            )
        )
    val processed =
        processEventList(
            trackChunk,
            MidiFormat.SINGLE,
            ppqn
        )
    assertEquals(
      listOf(
          EventSet(
              DEFAULT_MSPQN, listOf(
                  NoteOnEvent(
                      42,
                      100,
                      0
                  ),
                  NoteOnEvent(
                      42,
                      100,
                      1
                  )
              )
          ),
          EventSet(
              0, listOf(
                  NoteOffEvent(
                      42,
                      100,
                      0
                  ),
                  NoteOffEvent(
                      42,
                      100,
                      1
                  )
              )
          )
      ),
      processed
    )
  }


  @Test
  fun testGetSamples() {
    val input = listOf(
        EventSet(
            1000, listOf(
                ProgramChangeEvent(
                    40,
                    0,
                    "",
                    0
                ),
                NoteOnEvent(
                    42,
                    100,
                    0
                )
            )
        ),
        EventSet(
            0,
            listOf(
                NoteOffEvent(
                    42,
                    100,
                    0
                )
            )
        )
    )
    val samples =
        getSamples(
            input,
            StubSampler()
        )
    assertEquals(listOf(
        Sample(listOf()),
        Sample(listOf())

    ), samples)
  }

  @Test
  fun testGetSamplesTwoEntries() {
    val input = listOf(
        EventSet(
            500,
            listOf(
                ProgramChangeEvent(
                    40,
                    0,
                    "",
                    0
                )
            )
        ),
        EventSet(
            500,
            listOf(
                NoteOnEvent(
                    42,
                    100,
                    0
                )
            )
        ),
        EventSet(
            0,
            listOf(
                NoteOnEvent(
                    42,
                    100,
                    0
                )
            )
        )
    )
    val samples =
        getSamples(
            input,
            StubSampler()
        )
    assertEquals(listOf(
        Sample(
            listOf()
        ),
        Sample(listOf()),
        Sample(listOf())
    ), samples)
  }

  @Test
  fun testGetTempo() {
    val tempoTrack = listOf(
      0 to TempoEvent(
          500000
      )
    )
    val tempo = getTempo(
        tempoTrack,
        0
    )
    assertThat(tempo, `is`(500000L))
  }

  @Test
  fun testGetTempoTwoEvents() {
    val tempoTrack = listOf(
      0 to TempoEvent(
          500000
      ),
      1000 to TempoEvent(
          1000000
      )
    )
    val tempo = getTempo(
        tempoTrack,
        0
    )
    assertThat(tempo, `is`(500000L))
  }


  private class StubSampler :
      ISampler {
    override fun passEvent(midiEvent: MidiEvent) {
    }

    override fun getSample(length: Ms): List<Short> {
      return listOf()
    }

    override fun open() {

    }

    override fun close() {
    }
  }

}