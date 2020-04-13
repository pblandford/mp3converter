package com.philblandford.mp3convertercore.engine.file.output

data class RiffChunk(val chunkId:String = "RIFF", val chunkSize:Int, val format:String = "WAVE")

data class FmtChunk(val subChunk1Id:String ="fmt ", val subChunk1Size:Int = 16, val audioFormat:Short = 1,
    val numChannels:Short, val sampleRate:Int, val byteRate:Int, val blockAlign:Short,
    val bitsPerSample:Short)

data class DataChunk(val subChunk2Id:String = "data", val subChunk2Size:Int, val data:List<Short>)

data class WaveFile(val riffChunk: RiffChunk, val fmtChunk: FmtChunk, val dataChunk: DataChunk)

data class Mp3Header(val syncFlags:Short = 0xfffb.toShort(), val bitRateFrequency:Byte = 0x40.toByte(),
                     val modeCopy:Byte = 0x40.toByte())
data class Mp3Data(val bytes:List<Byte>)

data class HeaderDataPair(val mp3Header: Mp3Header, val mp3Data: Mp3Data)

data class Mp3File(val pairs:List<Mp3Data>)