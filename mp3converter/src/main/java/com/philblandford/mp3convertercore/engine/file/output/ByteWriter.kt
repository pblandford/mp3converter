package com.philblandford.mp3convertercore.engine.file.output

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class ByteWriter {
  private val byteList = mutableListOf<Byte>()

  fun writeByte(byte: Byte) {
    byteList.add(byte)
  }

  fun writeUInt(unsigned: UInt) {
    byteList.add((unsigned and 0xff.toUInt()).toByte())
    byteList.add(((unsigned shr 8) and 0xff.toUInt()).toByte())
    byteList.add(((unsigned shr 16) and 0xff.toUInt()).toByte())
    byteList.add(((unsigned shr 24) and 0xff.toUInt()).toByte())
  }

  fun writeUShort(unsigned: UShort) {
    byteList.add((unsigned.toUInt() and 0xff.toUInt()).toByte())
    byteList.add(((unsigned.toUInt() shr 8) and 0xff.toUInt()).toByte())
  }

  fun writeString(string: String) {
    string.forEach {
      byteList.add(it.toByte())
    }
  }

  fun writeBytes(bytes: Iterable<Byte>) {
    bytes.forEach { writeByte(it) }
  }

  fun writeClass(instance: Any) {
    val clazz = instance::class
    clazz.primaryConstructor?.parameters?.forEach { parameter ->
      clazz.memberProperties.find { it.name == parameter.name }?.let { memberProperty ->
        val cast = memberProperty as KProperty1<Any, *>
        writeValue(cast.get(instance))
      }
    }
  }

  private fun writeValue(value: Any?) {
    when (value) {
      is Byte -> writeByte(value)
      is Short -> writeUShort(value.toUShort())
      is Int -> writeUInt(value.toUInt())
      is String -> writeString(value)
      is List<*> -> (value as List<Any>).forEach {
        writeValue(it)
      }
      is Any -> writeClass(value)
    }
  }

  fun getBytes(): List<Byte> = byteList

}