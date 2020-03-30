package com.philblandford.mp3converter.engine.file.output

fun createMp3File(dataChunks:List<Mp3Data>):Mp3File {

  return Mp3File(dataChunks)
}