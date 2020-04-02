package com.philblandford.mp3converter.engine.file.input

import android.os.Environment
import com.philblandford.mp3converter.SAMPLE_RATE
import com.philblandford.mp3converter.engine.file.output.*
import com.philblandford.mp3converter.engine.sample.Delta
import com.philblandford.mp3converter.engine.sample.ISampler
import com.philblandford.mp3converter.engine.sample.Ms
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

typealias PairList<T> = List<Pair<Int, T>>
typealias PairListL<T> = List<Pair<Long, T>>
typealias MutablePairList<T> = MutableList<Pair<Int, T>>

data class EventSet(val duration: Ms, val events: List<MidiEvent>)
data class Sample(val samples: List<Short>)


fun <T> mutablePairListOf() = mutableListOf<Pair<Int, T>>()
fun <T> mutablePairListLOf() = mutableListOf<Pair<Long, T>>()

fun midiToWaveFile(
  midiFile: MidiFile,
  sampler: ISampler,
  updateProgress: (Int) -> Unit = {}
): Flow<ByteArray> {
  val tempoEvents = getTempoEvents(midiFile.trackChunk.tracks.first())

  val processedList = processEventList(
    midiFile.trackChunk, midiFile.header.format,
    midiFile.header.timingInterval
  ) {
    getTempo(tempoEvents, it)
  }
  val lengthMs = processedList.sumByDouble { it.duration.toDouble() }.toLong() + 50L
  val length = ((lengthMs * SAMPLE_RATE * 2)/1000).toInt()

  var wavFile = createWaveFile(listOf())
  wavFile = wavFile.copy(riffChunk = wavFile.riffChunk.copy(chunkSize = length / 2 + 44))
  val byteWriter = ByteWriter()
  byteWriter.writeClass(wavFile.riffChunk)
  byteWriter.writeClass(wavFile.fmtChunk)
  byteWriter.writeString("data")
  byteWriter.writeUInt(length.toUInt())

  return flow {
    emit(byteWriter.getBytes().toByteArray())
    processedList.withIndex().forEach { iv ->
      val percent = (iv.index.toFloat() / processedList.size) * 100
      updateProgress(percent.toInt())
      val eventSet = iv.value
      val sample = getSample(eventSet, sampler)
      val bytes = sample.samples.flatMap { short ->
        listOf(
          (short.toByte()),
          ((short.toUInt() shr 8) and 0xff.toUInt()).toByte()
        )
      }
      emit(bytes.toByteArray())
    }
  }
}


internal fun getSamples(eventSets: List<EventSet>, sampler: ISampler): List<Sample> {

  return runBlocking {
    eventSets.map { eventSet ->
      eventSet.events.forEach {
        sampler.passEvent(it)
      }
      Sample(sampler.getSample(eventSet.duration))
    }
  }
}


internal fun getSample(eventSet: EventSet, sampler: ISampler): Sample {

  eventSet.events.forEach {
    sampler.passEvent(it)
  }
  return Sample(sampler.getSample(eventSet.duration))
}


internal const val DEFAULT_MSPQN = 500000L
internal fun getTempo(tempoEvents: PairList<TempoEvent>, offset: Delta): Long {
  return tempoEvents.dropLastWhile { it.first >= offset }.lastOrNull()?.second?.msPerCrotchet
    ?: DEFAULT_MSPQN
}

internal fun deltaToMS(delta: Int, ppqn: Int, mspqn: Long): Long {
  val msPerTickMult = (mspqn.toDouble() / ppqn)
  val ret = (msPerTickMult * delta).toLong()
  return ret
}

internal fun processEventList(
  trackChunk: TrackChunk,
  format: MidiFormat,
  ppqn: Int,
  getTempo: (Delta) -> Long
): List<EventSet> {

  val tracks = when (format) {
    MidiFormat.SINGLE -> trackChunk.tracks
    else -> trackChunk.tracks.drop(1)
  }

  val allEvents = tracks.flatMap { track ->
    getTrackEventsWithOffsets(track.events)
  }.sortedBy { it.first }

  val eventSets = allEvents.groupBy { it.first }.map { (offset, events) ->
    offset to EventSet(0, events.map { it.second })
  }.sortedBy { it.first }

  return eventSets.setDurations(ppqn, getTempo)

}

internal fun getTrackEventsWithOffsets(
  list: PairList<MidiEvent>
): PairList<MidiEvent> {

  val results = mutablePairListOf<MidiEvent>()

  var ticksTotal = 0
  list.forEach { (deltaOffset, event) ->
    if (event.channel == 9) {
      println("Stop here")
    }
    ticksTotal += deltaOffset
    results.add(ticksTotal to event)
  }

  return results.sortedWith(compareBy({ it.first }, { it.second.priority }))
}

private fun PairList<EventSet>.setDurations(ppqn:Int, getTempo: (Delta) -> Long):List<EventSet> {

  val eventSetsTerm = this + (last().first to  EventSet(0, listOf()))
  return eventSetsTerm.windowed(2).map { pair ->
    val tempo = getTempo(pair[1].first)
    val durationTicks = pair[1].first - pair[0].first
    val ms = deltaToMS(durationTicks, ppqn, tempo)
    EventSet(ms, pair[0].second.events)
  }
}

private fun getTempoEvents(track: Track): PairList<TempoEvent> {

  var offset = 0
  val mapped = track.events.map { (delta, event) ->
    offset += delta
    offset to event
  }
  return mapped.filter { it.second is TempoEvent }.map { it.first to it.second as TempoEvent }
}