package com.philblandford.mp3convertercore.engine.file

import com.philblandford.mp3convertercore.engine.Settings
import com.philblandford.mp3convertercore.engine.encode.IEncoder
import com.philblandford.mp3convertercore.engine.file.input.*
import com.philblandford.mp3convertercore.engine.file.input.DEFAULT_MSPQN
import com.philblandford.mp3convertercore.engine.file.input.Left
import com.philblandford.mp3convertercore.engine.file.input.Right
import com.philblandford.mp3convertercore.engine.file.input.getSample
import com.philblandford.mp3convertercore.engine.file.input.processEventList
import com.philblandford.mp3convertercore.engine.file.input.*
import com.philblandford.mp3convertercore.engine.sample.Delta
import com.philblandford.mp3convertercore.engine.sample.ISampler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

fun convertMidiToWave(
  midiData: ByteArray,
  sampler: ISampler,
  settings: Settings = Settings(),
  updateProgress: (Int) -> Unit = {}
): Flow<ByteArray> {

  return when (val res =
    readMidiFile(
      midiData.toList()
    )) {
    is Right -> midiToWaveFile(
      res.r,
      sampler,
      settings,
      updateProgress
    )
    is Left -> throw Exception(Exception("Read Midi failed at ${res.l.byte} with ${res.l.message}"))
  }
}

fun convertMidiToMp3(
  midiData: ByteArray, sampler: ISampler, encoder: IEncoder, settings:Settings, updateProgress: (Int) -> Unit = {}
): Flow<ByteArray> {
  return when (val res =
    readMidiFile(
      midiData.toList()
    )) {
    is Right -> {
      val flow =
        writeMidiSamples(
          res.r,
          sampler,
          encoder,
          settings,
          updateProgress
        )
      flow
    }
    is Left -> throw(Exception("Read Midi failed at ${res.l.byte} with ${res.l.message}"))
  }
}

private fun writeMidiSamples(
  midiFile: MidiFile,
  sampler: ISampler,
  encoder: IEncoder,
  settings:Settings,
  updateProgress: (Int) -> Unit
): Flow<ByteArray> {

  val processedList =
    processEventList(
      midiFile.trackChunk, midiFile.header.format, midiFile.header.timingInterval
    )
  sampler.open(settings.sampleRate)
  val ret = flow {
    processedList.withIndex().forEach { iv ->
      val percent = (iv.index.toFloat() / processedList.size) * 100
      updateProgress(percent.toInt())
      val eventSet = iv.value
      val sample =
        getSample(
          eventSet,
          sampler,
          settings
        )
      val buf = encoder.encodeSample(sample.samples).toByteArray()
      emit(buf)
    }
  }
  sampler.close()
  return ret
}

