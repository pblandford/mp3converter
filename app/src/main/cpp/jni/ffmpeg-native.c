//
// Created by philb on 24/03/20.
//

#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>

#include "lame.h"

lame_global_flags *gfp;


JNIEXPORT void JNICALL
Java_com_philblandford_mp3converter_engine_encode_LameEncoderKt_init(JNIEnv
                                                                     *env,
                                                                     jobject thiz) {
    gfp = lame_init();
    lame_init_params(gfp);
}


JNIEXPORT void JNICALL
Java_com_philblandford_mp3converter_engine_encode_LameEncoderKt_close(JNIEnv
                                                                      *env,
                                                                      jobject thiz) {
    if (gfp != NULL) {
        lame_close(gfp);
    }
}

JNIEXPORT jbyteArray JNICALL
Java_com_philblandford_mp3converter_engine_encode_LameEncoderKt_encodeBytes(JNIEnv
                                                                            *env,
                                                                            jobject thiz,
                                                                            jshortArray sampleArray,
                                                                            jint numSamples) {
    short *input = (*env)->GetShortArrayElements(env, sampleArray, NULL);

    int buffSize = 1.25 * numSamples + 7200;
    unsigned char *mp3buffer = (unsigned char *) calloc(1, buffSize);

    int total = lame_encode_buffer(gfp, input, input, numSamples, mp3buffer, buffSize);

    if (total < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "LM", "Error encoding buffer %d", total);
        free(mp3buffer);
        return NULL;
    }


    jbyteArray array = (*env)->NewByteArray(env, total);
    (*env)->SetByteArrayRegion(env, array, 0, total, (jbyte *) mp3buffer);

    free(mp3buffer);
    return array;
}
