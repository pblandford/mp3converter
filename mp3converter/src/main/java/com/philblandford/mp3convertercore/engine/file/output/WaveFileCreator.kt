package com.philblandford.mp3convertercore.engine.file.output

import com.philblandford.mp3convertercore.engine.BITS
import com.philblandford.mp3convertercore.engine.CHANNELS
import com.philblandford.mp3convertercore.engine.SAMPLE_RATE
import com.philblandford.mp3convertercore.engine.file.input.Sample

interface IWaveFileCreator {
  fun createWaveFile()
}

fun createWaveFile(samples: List<Sample>): WaveFile {
  val shorts =
      getPCMShorts(
          samples
      )
  return WaveFile(
      RiffChunk(
          chunkSize = 44 + shorts.size
      ),
      FmtChunk(
          numChannels = 1,
          sampleRate = SAMPLE_RATE,
          byteRate = SAMPLE_RATE * CHANNELS * (BITS / 8),
          blockAlign = (CHANNELS * (BITS / 8)).toShort(),
          bitsPerSample = BITS.toShort()
      ),
      DataChunk(
          subChunk2Size = shorts.size * 2,
          data = shorts
      )
  )
}


internal fun getPCMShorts(samples: List<Sample>): List<Short> {
  return samples.flatMap { it.samples }
}