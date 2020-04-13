package com.philblandford.mp3converter.engine.file

import android.content.Context
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import com.philblandford.mp3convertercore.engine.encode.IEncoder
import com.philblandford.mp3convertercore.engine.encode.LameEncoder
import com.philblandford.mp3convertercore.engine.file.convertMidiToMp3
import com.philblandford.mp3convertercore.engine.file.convertMidiToWave
import com.philblandford.mp3convertercore.engine.sample.FluidSampler
import com.philblandford.mp3convertercore.engine.sample.ISampler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.Test

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MidiToWaveConverterTest {

  private val filename = "2pac"

  private val context: Context = InstrumentationRegistry.getInstrumentation().context

  @Test
  fun testConvertMidiToWave() {
    val sampler = getSampler()
    val midiBytes = getMidiFile()
    val outputStream = getOutputStream("wav")
    val output =
        convertMidiToWave(
            midiBytes,
            sampler
        )

    runBlocking {
      withContext(Dispatchers.IO) {
        output.collect { value ->
          outputStream.write(value)
        }
      }
    }

    outputStream.close()
  }

  @Test
  fun testConvertMidiToMp3BytesWritten() {
    val sampler = getSampler()
    val encoder = getEncoder()
    val outputStream = getOutputStream("mp3")

    val midiBytes = getMidiFile()
    val output =
        convertMidiToMp3(
            midiBytes,
            sampler,
            encoder
        )
    runBlocking {
      withContext(Dispatchers.IO) {
        output.collect { value ->
          outputStream.write(value)
        }
      }
    }

    outputStream.close()
  }

  private fun getSampler(): ISampler {
    val inputStream = context.resources.assets.open("chaos.sf2")
    val sfFile = File(Environment.getExternalStorageDirectory(), "tmpfile.sf2")
    FileUtils.copyInputStreamToFile(inputStream, sfFile)
    return FluidSampler(
        sfFile.absolutePath
    )
  }

  private fun getEncoder(): IEncoder {
    return LameEncoder();
  }

  private fun getMidiFile(): ByteArray {
    val input =
      InstrumentationRegistry.getInstrumentation().context.resources.assets.open("$filename.mid")
    return IOUtils.toByteArray(input)
  }

  private fun writeOutput(byteArray: ByteArray, extension: String) {
    val dir = Environment.getExternalStorageDirectory()
    val file = File(dir, "$filename.$extension")
    FileUtils.writeByteArrayToFile(file, byteArray)
  }

  private fun getOutputStream(extension: String): OutputStream {
    val dir = Environment.getExternalStorageDirectory()
    val file = File(dir, "$filename.$extension")
    return FileOutputStream(file)
  }
}