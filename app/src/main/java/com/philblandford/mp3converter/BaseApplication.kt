package com.philblandford.mp3converter

import android.app.Application
import android.os.Environment
import com.philblandford.mp3converter.engine.encode.IEncoder
import com.philblandford.mp3converter.engine.encode.LameEncoder
import com.philblandford.mp3converter.engine.sample.FluidSampler
import com.philblandford.mp3converter.engine.sample.ISampler
import com.philblandford.mp3converter.repository.FileConverter
import com.philblandford.mp3converter.repository.MediaFileGetter
import org.apache.commons.io.FileUtils
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

class BaseApplication : Application() {

  override fun onCreate() {
    initKoin()
    super.onCreate()
  }

  private fun initKoin() {
    val sampler = getSampler()
    val module = module {
      single<FileGetter> { MediaFileGetter(contentResolver) }
      single<Converter> { FileConverter(contentResolver) }
      single<IEncoder> { LameEncoder() }
      single { sampler }
    }
    startKoin { modules(module) }
  }

  private fun getSampler(): ISampler {
    val inputStream = resources.assets.open("chaos.sf2")
    val sfFile = File(cacheDir, "tmpfile.sf2")
    FileUtils.copyInputStreamToFile(inputStream, sfFile)
    return FluidSampler(sfFile.absolutePath)
  }
}