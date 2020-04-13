package com.philblandford.mp3converter.engine.file

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.philblandford.mp3convertercore.engine.file.input.Left
import com.philblandford.mp3convertercore.engine.file.input.Right
import com.philblandford.mp3convertercore.engine.file.input.readMidiFile
import org.apache.commons.io.IOUtils
import org.junit.Test

class MidiFileReaderTest {

  @Test
  fun testTryImportFile() {
    val input = getInstrumentation().context.resources.assets.open("Queen.mid")
    val bytes = IOUtils.toByteArray(input).toList()
    val either =
        readMidiFile(
            bytes
        )
    when (either) {
      is Left -> throw Exception(either.toString())
      is Right -> {

      }
    }
    assert(either is Right)
  }
}