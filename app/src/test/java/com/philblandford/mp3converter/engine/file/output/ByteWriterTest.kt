package com.philblandford.mp3converter.engine.file.output

import junit.framework.Assert.assertEquals
import org.junit.Test

class ByteWriterTest {

  @Test
  fun testWriteByte() {
    val br = ByteWriter()
    br.writeByte(0xef.toByte())
    assertEquals(listOf(0xef.toByte()), br.getBytes())
  }

  @Test
  fun testWriteClassByteList() {
    data class TestClass(val bytes:List<Byte>)
    val testClass = TestClass(listOf(0xef.toByte()))
    val br = ByteWriter()
    br.writeClass(testClass)
    assertEquals(listOf(0xef.toByte()), br.getBytes())
  }
}