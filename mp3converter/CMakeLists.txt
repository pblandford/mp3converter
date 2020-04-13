cmake_minimum_required(VERSION 3.4.1)


add_library(fluidsynth
        SHARED
        IMPORTED)

set(lib_PATH ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})
set(native_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp/jni)
set(fluid_source_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp/fluidsynth)
set(lame_source_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp/lame)


set_target_properties(
        fluidsynth
        PROPERTIES IMPORTED_LOCATION
        ${lib_PATH}/libfluidsynth.so)

add_library(fluid-native
        SHARED
        ${native_PATH}/fluid-native.c)



file(GLOB lame_files ${lame_source_PATH}/libmp3lame/*.c)

include_directories(${fluid_source_PATH} ${fluid_source_PATH}/include ${ffmpeg_source_PATH} ${lame_source_PATH}/include)

add_library(ffmpeg-native
        SHARED
        ${native_PATH}/ffmpeg-native.c)

add_definitions(-DSTDC_HEADERS)
add_library(lame
        SHARED
        ${lame_files}
        )

target_link_libraries(ffmpeg-native
        lame
        android
        log)

target_link_libraries(fluid-native
        fluidsynth
        android
        log)

