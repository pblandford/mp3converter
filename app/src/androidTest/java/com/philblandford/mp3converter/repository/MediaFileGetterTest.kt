package com.philblandford.mp3converter.repository

import android.database.Cursor
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.philblandford.mp3converter.ExportType
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*

class MediaFileGetterTest {

  private lateinit var mediaFileGetter: MediaFileGetter

  @Before
  fun setUp() {
    val contentResolver = InstrumentationRegistry.getInstrumentation().context.contentResolver
    mediaFileGetter = MediaFileGetter(contentResolver)
  }

  @After
  fun tearDown() {
  }

  @Test
  fun getMidiFiles() {
    mediaFileGetter.clear()
    mediaFileGetter.createNewFile("Wibble.mid", ExportType.MIDI)?.let { desc ->
      desc.outputStream.use { os ->
        os.write("Wibble wobble".toByteArray())
        mediaFileGetter.finishSave(desc.uri)
      }

      val files = mediaFileGetter.getMidiFiles().map { it.name }
      assertEquals(listOf("Wibble.mid"), files)
    } ?: run {
      throw Exception("Could not get output stream")
    }
  }

  @Test
  fun createNewFile() {
  }



  private val midiFiles = listOf("One.mid", "Two.mid", "Three.mid")
}