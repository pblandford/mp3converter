package com.philblandford.mp3convertercore.engine.file.input

import com.philblandford.mp3convertercore.engine.file.input.ByteReader
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test

class ByteReaderTest {

  @Test
  fun testReadByte() {
    val br =
        ByteReader(
            byteArrayOf(0xfe.toByte())
        )
    val ret = br.getNextByte()
    assertThat(ret, `is`(0xfe.toUByte()))
  }

  @Test
  fun testReadByteTwice() {
    val br = ByteReader(
        byteArrayOf(
            0xfe.toByte(),
            0xad.toByte()
        )
    )
    var ret = br.getNextByte()
    assertThat(ret, `is`(0xfe.toUByte()))
    ret = br.getNextByte()
    assertThat(ret, `is`(0xad.toUByte()))
  }

  @Test
  fun testReadShort() {
    val br = ByteReader(
        byteArrayOf(
            0xfe.toByte(),
            0xad.toByte()
        )
    )
    val ret = br.getNextShort()
    assertThat(ret, `is`(0xfead.toUShort()))
  }

  @Test
  fun testRead24bits() {
    val br = ByteReader(
        byteArrayOf(
            0xab.toByte(),
            0xcd.toByte(),
            0xef.toByte()
        )
    )
    val ret = br.getNext24Bit()
    assertThat(ret, `is`(0xabcdef.toUInt()))
  }


  @Test
  fun testReadInt() {
    val br = ByteReader(
        byteArrayOf(
            0xfe.toByte(),
            0xad.toByte(),
            0x43.toByte(),
            0x92.toByte()
        )
    )
    val ret = br.getNextInt()
    assertThat(ret, `is`(0xfead4392.toUInt()))
  }

  @Test
  fun testReadString() {
    val br =
        ByteReader("MTrk".toByteArray())
    val ret = br.getNextString(4)
    assertThat(ret, `is`("MTrk"))
  }
}