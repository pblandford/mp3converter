package com.philblandford.mp3converter.repository

import android.content.ContentResolver
import com.philblandford.mp3converter.Converter
import com.philblandford.mp3converter.ExportType
import com.philblandford.mp3converter.MidiFileDescr
import com.philblandford.mp3converter.engine.encode.IEncoder
import com.philblandford.mp3converter.engine.file.convertMidiToMp3
import com.philblandford.mp3converter.engine.file.convertMidiToWave
import com.philblandford.mp3converter.engine.sample.ISampler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.FileInputStream
import java.io.OutputStream

class FileConverter(private val contentResolver: ContentResolver) : Converter, KoinComponent {

  private val encoder: IEncoder by inject()
  private val sampler: ISampler by inject()

  override suspend fun convertFile(
    midiFile: MidiFileDescr,
    exportType: ExportType,
    outputStream: OutputStream,
    updateProgress: (Int)->Unit
  ) {
    withContext(Dispatchers.IO) {
      contentResolver.openFileDescriptor(midiFile.uri, "r")?.use { pfd ->
        val fis = FileInputStream(pfd.fileDescriptor)
        val bytes = IOUtils.toByteArray(fis)
        when (exportType) {
          ExportType.MP3 -> convertMidiToMp3(bytes, sampler, encoder, updateProgress).collect { byteArray ->
            outputStream.write(byteArray)
          }
          ExportType.WAV -> convertMidiToWave(bytes, sampler, updateProgress).collect() { byteArray ->
            outputStream.write(byteArray)
          }
          else -> {
          }
        }
      }
    }
  }
}