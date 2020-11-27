package com.philblandford.mp3convertercore.engine.encode

interface IEncoder {
  fun encodeSample(sample:List<Short>):List<Byte>
  fun start(sampleRate:Int, bitRate: Int)
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

  override fun start(sampleRate: Int, bitRate: Int) {
      init(sampleRate, bitRate)
  }

  override fun finish() {
      close()
  }
}

external fun encodeBytes(samples:ShortArray, num:Int):ByteArray?
external fun init(sampleRate: Int, bitRate:Int);
external fun close();