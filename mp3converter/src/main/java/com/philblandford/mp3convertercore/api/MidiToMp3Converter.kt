package com.philblandford.mp3convertercore.api

import com.philblandford.mp3convertercore.engine.Settings
import com.philblandford.mp3convertercore.engine.encode.IEncoder
import com.philblandford.mp3convertercore.engine.encode.LameEncoder
import com.philblandford.mp3convertercore.engine.file.convertMidiToMp3
import com.philblandford.mp3convertercore.engine.file.convertMidiToWave
import com.philblandford.mp3convertercore.engine.sample.FluidSampler
import com.philblandford.mp3convertercore.engine.sample.ISampler
import kotlinx.coroutines.flow.collect
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception

enum class ExportType {
  MP3,
  WAV,
  MIDI
}

class MidiToMp3Converter(soundFontPath: String) {

  private val encoder: IEncoder =
    LameEncoder()
  private val sampler: ISampler =
    FluidSampler(
      soundFontPath
    )

  suspend fun convert(
    inputStream: InputStream,
    outputStream: OutputStream,
    exportType: ExportType,
    settings: Settings,
    updateProgress: (Int) -> Unit,
    onFail: (ByteArray, Exception) -> Unit,
  ) {
    val bytes = IOUtils.toByteArray(inputStream)
    try {
      doConvert(bytes, outputStream, exportType, settings, updateProgress)
    } catch (e: Exception) {
      onFail(bytes, e)
    }

  }

  private suspend fun doConvert(bytes: ByteArray,
                                outputStream: OutputStream,
                                exportType: ExportType,
                                settings: Settings,
                                updateProgress: (Int) -> Unit) {
    when (exportType) {
      ExportType.MP3 -> {
        encoder.start(settings.sampleRate, settings.bitRate)
        convertMidiToMp3(
          bytes,
          sampler,
          encoder,
          settings,
          updateProgress
        ).collect { byteArray ->
          outputStream.write(byteArray)
        }
        encoder.finish()
      }
      ExportType.WAV -> convertMidiToWave(
        bytes,
        sampler,
        settings,
        updateProgress
      ).collect { byteArray ->
        outputStream.write(byteArray)
      }
      else -> {
      }
    }
  }


  fun cancel() {
    // encoder.finish()
    sampler.close()
  }

}