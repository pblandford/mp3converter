package com.philblandford.mp3converter.engine.file.output

import com.philblandford.mp3converter.BITS
import com.philblandford.mp3converter.SAMPLE_RATE
import com.philblandford.mp3converter.engine.file.input.Sample
import org.junit.Assert.*
import org.junit.Test

const val MILLISECONDS_WORTH = (SAMPLE_RATE * (BITS / 8))/1000

class WaveFileCreatorTest {

  @Test
  fun testGetPCMBytesOneSample() {
    val samples = listOf(Sample(junkBytes(100)))
    val pcmBytes = getPCMShorts(samples)
    assertEquals(junkBytes(100), pcmBytes)
  }

  @Test
  fun testGetPCMBytesTwoSamples() {
    val samples = listOf(
      Sample(junkBytes(100)),
      Sample(junkBytes(50))
    )
    val pcmBytes = getPCMShorts(samples)
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