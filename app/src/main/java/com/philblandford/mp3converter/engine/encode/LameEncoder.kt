package com.philblandford.mp3converter.engine.encode

interface IEncoder {
  fun encodeSample(sample:List<Short>):List<Byte>
}

class LameEncoder : IEncoder {

  init {
    System.loadLibrary("ffmpeg-native")
    init();
  }

  override fun encodeSample(sample: List<Short>): List<Byte> {
    return encodeBytes(sample.toShortArray(), sample.size)?.toList() ?: listOf()
  }
}

external fun encodeBytes(samples:ShortArray, num:Int):ByteArray?
external fun init();