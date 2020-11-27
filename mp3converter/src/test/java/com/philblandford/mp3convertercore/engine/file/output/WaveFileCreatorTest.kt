package com.philblandford.mp3convertercore.engine.file.output

import com.philblandford.mp3convertercore.engine.BITS
import com.philblandford.mp3convertercore.engine.CUSTOM_SAMPLE_RATE
import com.philblandford.mp3convertercore.engine.file.input.Sample
import org.junit.Assert.*
import org.junit.Test

class WaveFileCreatorTest {

  @Test
  fun testGetPCMBytesOneSample() {
    val samples = listOf(
        Sample(
            junkBytes(100)
        )
    )
    val pcmBytes =
        getPCMShorts(
            samples
        )
    assertEquals(junkBytes(100), pcmBytes)
  }

  @Test
  fun testGetPCMBytesTwoSamples() {
    val samples = listOf(
        Sample(
            junkBytes(
                100
            )
        ),
        Sample(
            junkBytes(
                50
            )
        )
    )
    val pcmBytes =
        getPCMShorts(
            samples
        )
    val expected = junkBytes(150)
    assertEquals(expected, pcmBytes)
  }

  private fun junkBytes(num: Int): List<Short> {
    return (1..num).map { 0xffff.toShort() }
  }

  private fun emptyBytes(num: Int): List<Byte> {
    return (1..num).map { 0.toByte() }
  }
}