//
// Created by philb on 15/03/20.
//

#include <string.h>
#include <jni.h>
#include <stdlib.h>
#include <android/log.h>

#include "fluid_synth.h"


fluid_settings_t *settings;
fluid_synth_t *synth;


int64_t SAMPLE_RATE = (int64_t) 44100;

JNIEXPORT void JNICALL
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_openFluid(JNIEnv
                                                                    *env,
                                                                    jobject thiz, jstring
                                                                    soundfontPath) {
    const char *nativeString = (*env)->GetStringUTFChars(env, soundfontPath, NULL);

    __android_log_print(ANDROID_LOG_DEBUG,
                        "FLD", "Initialising fluidsynth %s", nativeString);

    settings = new_fluid_settings();
    synth = new_fluid_synth(settings);
    fluid_synth_sfload(synth, nativeString,
                       1);
    (*env)->
            ReleaseStringUTFChars(env, soundfontPath, nativeString
    );
}

JNIEXPORT jshortArray
Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_getSampleData(JNIEnv
                                                                        *env,
                                                                        jclass thiz,
                                                                        jlong numShorts) {

    short *result = (short *) malloc(numShorts * 2);
//    __android_log_print(ANDROID_LOG_DEBUG,
//                        "FLD", "getting buffer of size %lld %p %p", numShorts, result, *env);

    fluid_synth_write_s16(synth, numShorts, result,
                          0, 1, result, 0, 1);
//    __android_log_print(ANDROID_LOG_DEBUG,
//                        "FLD", "got buffer of size %lld", numShorts);

    jshortArray retArray = (*env)->NewShortArray(env, numShorts);
    (*env)->
            SetShortArrayRegion(env, retArray,
                                0, numShorts, result);
    free(result);

    return
            retArray;
}

JNIEXPORT void

JNICALL Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_programChange(JNIEnv *env,
                                                                                jclass thiz,
                                                                                jint channel,
                                                                                jint midiId) {
    int ret = fluid_synth_program_change(synth, channel, midiId);
    if (ret != FLUID_OK) {
        __android_log_write(ANDROID_LOG_ERROR, "FLD", "Failed changing program");
    }
}


JNIEXPORT void

JNICALL Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_noteOn(JNIEnv *env,
                                                                         jclass thiz,
                                                                         jint channel, jint midiVal,
                                                                         jint velocity) {
    int ret = fluid_synth_noteon(synth, channel, midiVal, velocity);
    if (ret != FLUID_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "FLD", "Failed note on %d %d", midiVal, velocity);
    }
}

JNIEXPORT void

JNICALL Java_com_philblandford_mp3converter_engine_sample_FluidSamplerKt_noteOff(JNIEnv *env,
                                                                          jclass thiz, jint channel,
                                                                          jint midiVal) {
    int ret = fluid_synth_noteoff(synth, channel, midiVal);
    if (ret != FLUID_OK) {
        __android_log_write(ANDROID_LOG_ERROR, "FLD", "Failed note off");
    }
}