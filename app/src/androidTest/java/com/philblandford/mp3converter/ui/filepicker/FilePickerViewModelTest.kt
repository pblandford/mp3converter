package com.philblandford.mp3converter.ui.filepicker

import android.content.Context
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.philblandford.mp3converter.Converter
import com.philblandford.mp3converter.FileGetter
import com.philblandford.mp3converter.MidiFileDescr
import com.philblandford.mp3converter.stubs.StubConverter
import com.philblandford.mp3converter.stubs.StubFileGetter
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class FilePickerViewModelTest {

  private val context = InstrumentationRegistry.getInstrumentation().context
  private val fileGetter = StubFileGetter(context)

  @Before
  fun setup() {
    stopKoin()
    initKoin(context)
  }

  @After
  fun tearDown() {
    stopKoin()
  }

  @Test
  fun testGetFiles() {
    val expected = StubFileGetter(context).getMidiFiles()
    val received = FilePickerViewModel().getFileNames()
    assertEquals(expected, received)
  }


  private fun initKoin(context: Context) {
    val module = module {
      single<FileGetter> { fileGetter }
      single<Converter> { StubConverter() }
    }
    startKoin { modules(module) }
  }
}