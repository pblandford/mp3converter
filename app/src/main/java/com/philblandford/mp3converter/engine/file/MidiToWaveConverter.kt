package com.philblandford.mp3converter.engine.file

import com.philblandford.mp3converter.engine.encode.IEncoder
import com.philblandford.mp3converter.engine.file.input.*
import com.philblandford.mp3converter.engine.file.input.Left
import com.philblandford.mp3converter.engine.file.input.Right
import com.philblandford.mp3converter.engine.sample.Delta
import com.philblandford.mp3converter.engine.sample.ISampler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

fun convertMidiToWave(
  midiData: ByteArray,
  sampler: ISampler,
  updateProgress: (Int) -> Unit = {}
): Flow<ByteArray> {

  return when (val res = readMidiFile(midiData.toList())) {
    is Right -> midiToWaveFile(res.r, sampler, updateProgress)
    is Left -> throw Exception(Exception("Read Midi failed at ${res.l.byte} with ${res.l.message}"))
  }
}

fun convertMidiToMp3(
  midiData: ByteArray, sampler: ISampler, encoder: IEncoder, updateProgress: (Int) -> Unit = {}
): Flow<ByteArray> {
  return when (val res = readMidiFile(midiData.toList())) {
    is Right -> {
      val flow = writeMidiSamples(res.r, sampler, encoder, updateProgress)
      flow
    }
    is Left -> throw(Exception("Read Midi failed at ${res.l.byte} with ${res.l.message}"))
  }
}

private fun writeMidiSamples(
  midiFile: MidiFile,
  sampler: ISampler,
  encoder: IEncoder,
  updateProgress: (Int) -> Unit
): Flow<ByteArray> {

  val tempoFunc = getTempoFunc(midiFile)
  val processedList = processEventList(
    midiFile.trackChunk, midiFile.header.format, midiFile.header.timingInterval,
    tempoFunc
  )
  sampler.open()
  val ret = flow {
    processedList.withIndex().forEach { iv ->
      val percent = (iv.index.toFloat() / processedList.size) * 100
      updateProgress(percent.toInt())
      val eventSet = iv.value
      val sample = getSample(eventSet, sampler)
      val buf = encoder.encodeSample(sample.samples).toByteArray()
      emit(buf)
    }
  }
  sampler.close()
  return ret
}

private fun getTempoFunc(midiFile: MidiFile): (Delta) -> Long {
  return midiFile.trackChunk.tracks.find { it.events.any { it.second is TempoEvent } }
    ?.let { tempoTrack ->
      { input: Int -> getTempo(tempoTrack, input) }
    } ?: { _ -> 120 }
}

private fun getTempo(tempoTrack: Track, offset: Delta): Long {
  val tempoEvents =
    tempoTrack.events.filter { it.second is TempoEvent }.map { it as Pair<Int, TempoEvent> }
  return tempoEvents.dropLastWhile { it.first >= offset }.lastOrNull()?.second?.msPerCrotchet
    ?: DEFAULT_MSPQN
}