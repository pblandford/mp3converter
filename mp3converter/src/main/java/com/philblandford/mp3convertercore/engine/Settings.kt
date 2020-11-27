package com.philblandford.mp3convertercore.engine

data class Settings(val channels: Int = CHANNELS,
                    val sampleRate: Int = CD_SAMPLE_RATE,
                    val bitDepth: Int = BITS,
                    val bitRate: Int = BIT_RATE)

const val CD_SAMPLE_RATE = 44100
const val CHANNELS = 1
const val CUSTOM_SAMPLE_RATE = 48000
const val BITS = 16
const val BIT_RATE = 320