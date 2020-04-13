package com.philblandford.mp3convertercore.engine.file.input

class ByteReader(private val bytes: ByteArray) {

  private val mutableBytes = bytes.map { it.toUByte() }.toMutableList()

  fun getIdx() = bytes.size - mutableBytes.size
  fun getIdxLastInt() = getIdx() - 4
  fun getIdxLastShort() = getIdx() - 2
  fun getIdxLastByte() = getIdx() - 1

  fun peekNextByte():UByte = mutableBytes.first()

  fun getNextByte(): UByte {
    val ret = mutableBytes.first()
    mutableBytes.removeAt(0)
    return ret
  }

  fun getNextShort(): UShort {
    val sub = mutableBytes.subList(0, 2)
    val ret = sub.toList()
    sub.clear()
    return ((ret[0].toUInt() shl 8) or (ret[1].toUInt())).toUShort()
  }

  fun getNext24Bit(): UInt {
    val sub = mutableBytes.subList(0, 3)
    val ret = sub.toList()
    sub.clear()
    return ((ret[0].toUInt() shl 16) or (ret[1].toUInt() shl 8) or (ret[2].toUInt())).toUInt()
  }

  fun getNextInt(): UInt {
    val sub = mutableBytes.subList(0, 4)
    val ret = sub.toList()
    sub.clear()

    return (
      (ret[0].toUInt() shl 24) or
        (ret[1].toUInt() shl 16) or
        (ret[2].toUInt() shl 8) or
        ret[3].toUInt()
      )
  }

  fun getNextString(len: Int): String {
    val sub = mutableBytes.subList(0, len)
    val ret = sub.toList()
    sub.clear()
    return String(ret.toUByteArray().toByteArray())
  }
}