package com.philblandford.mp3converter

import android.content.Context
import android.os.Environment
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.philblandford.mp3convertercore.engine.BITS
import com.philblandford.mp3convertercore.engine.CHANNELS
import com.philblandford.mp3convertercore.engine.CUSTOM_SAMPLE_RATE
import com.philblandford.mp3convertercore.engine.file.output.*
import com.philblandford.mp3convertercore.engine.sample.FluidSampler
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File


class SampleTest {

  @get:Rule
  val instantExecutorRule = InstantTaskExecutorRule()

  private lateinit var context: Context

  @Before
  fun setup() {
    context = InstrumentationRegistry.getInstrumentation().context
  }

  @Test
  fun testGetSample() {
    val inputStream = context.resources.assets.open("chaos.sf2")
    val sfFile = File(Environment.getExternalStorageDirectory(), "tmpfile.sf2")
    FileUtils.copyInputStreamToFile(inputStream, sfFile)
    val sampler =
        FluidSampler(sfFile.absolutePath)
    val bytes = runBlocking {
      sampler.getSample( 1000, CUSTOM_SAMPLE_RATE)
    }
    val wavFile =
        WaveFile(
            RiffChunk(
                chunkSize = 44 + bytes.size
            ),
            FmtChunk(
                numChannels = 1,
                sampleRate = CUSTOM_SAMPLE_RATE,
                byteRate = CUSTOM_SAMPLE_RATE * CHANNELS * (BITS / 8),
                blockAlign = (CHANNELS * (BITS / 8)).toShort(),
                bitsPerSample = BITS.toShort()
            ),
            DataChunk(
                subChunk2Size = bytes.size,
                data = bytes
            )
        )
    val byteWriter =
        ByteWriter()
    byteWriter.writeClass(wavFile)
    val dir = Environment.getExternalStorageDirectory()
    val file = File(dir, "example.wav")
    FileUtils.writeByteArrayToFile(file, byteWriter.getBytes().toByteArray())
  }
}