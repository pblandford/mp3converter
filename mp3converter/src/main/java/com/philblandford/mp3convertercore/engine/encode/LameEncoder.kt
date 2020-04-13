package com.philblandford.mp3convertercore.engine.encode

interface IEncoder {
  fun encodeSample(sample:List<Short>):List<Byte>
  fun start()
  fun finish()
}

class LameEncoder :
    IEncoder {

  init {
    System.loadLibrary("ffmpeg-native")
  }

  override fun encodeSample(sample: List<Short>): List<Byte> {
    return encodeBytes(
        sample.toShortArray(),
        sample.size
    )?.toList() ?: listOf()
  }

  override fun start() {
      init()
  }

  override fun finish() {
      close()
  }
}

external fun encodeBytes(samples:ShortArray, num:Int):ByteArray?
external fun init();
external fun close();