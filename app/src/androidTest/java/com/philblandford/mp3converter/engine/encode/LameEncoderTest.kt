package com.philblandford.mp3converter.engine.encode

import com.philblandford.mp3convertercore.engine.encode.LameEncoder
import org.junit.Test

class LameEncoderTest {

  @Test
  fun testLibraryLoads() {
      LameEncoder()
  }

  @Test
  fun testEncodeSample() {
    val encoder =
        LameEncoder()
    val samples = getSamples(2048)
    val bytes = encoder.encodeSample(samples)
    assert(bytes.isNotEmpty())
  }

  private fun getSamples(len:Int):List<Short> {
    return (1..len).map { 0xffff.toShort() }
  }
}