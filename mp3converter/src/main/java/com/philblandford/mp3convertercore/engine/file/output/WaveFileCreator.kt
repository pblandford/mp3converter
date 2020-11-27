package com.philblandford.mp3convertercore.engine.file.output

import com.philblandford.mp3convertercore.engine.BITS
import com.philblandford.mp3convertercore.engine.CHANNELS
import com.philblandford.mp3convertercore.engine.Settings
import com.philblandford.mp3convertercore.engine.file.input.Sample

interface IWaveFileCreator {
  fun createWaveFile()
}

fun createWaveFile(samples: List<Sample>, settings: Settings): WaveFile {
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
          sampleRate = settings.sampleRate,
          byteRate = settings.sampleRate * CHANNELS * (settings.bitDepth / 8),
          blockAlign = (CHANNELS * (settings.bitDepth / 8)).toShort(),
          bitsPerSample = settings.bitDepth.toShort()
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