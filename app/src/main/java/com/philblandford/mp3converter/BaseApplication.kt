package com.philblandford.mp3converter

import android.app.Application
import com.philblandford.mp3convertercore.Converter
import com.philblandford.mp3convertercore.FileGetter
import com.philblandford.mp3converter.repository.FileConverter
import com.philblandford.mp3converter.repository.MediaFileGetter
import org.apache.commons.io.FileUtils
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

class BaseApplication : Application() {

  private lateinit var soundFontPath:String

  override fun onCreate() {
    soundFontPath = getSoundFontPath()
    initKoin()
    super.onCreate()
  }

  private fun initKoin() {
    val module = module {
      single<FileGetter> { MediaFileGetter(contentResolver, applicationContext) }
      single<Converter> { FileConverter(contentResolver, soundFontPath) }
    }
    startKoin { modules(module) }
  }

  private fun getSoundFontPath(): String {
    val inputStream = resources.assets.open("chaos.sf2")
    val sfFile = File(cacheDir, "tmpfile.sf2")
    FileUtils.copyInputStreamToFile(inputStream, sfFile)
    return sfFile.absolutePath
  }
}