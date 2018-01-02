set(CMAKE_VERBOSE_MAKEFILE on)


cmake_minimum_required(VERSION 3.4.1)
add_definitions(-DOS_ANDROID)

set(THIRDPARTY ${CMAKE_SOURCE_DIR}/../thirdparty)
set(OPENCV_DIR ${THIRDPARTY}/opencv-3.3.1-android-sdk)
set(DLIB_DIR ${THIRDPARTY}/dlib-19.8)
set(TF_DIR ${THIRDPARTY}/tensorflow-1.4.1)

set(CMAKE_ANDROID_STL_TYPE gnustl_static)
set(USE_SSE2_INSTRUCTIONS ON)
set(USE_SSE4_INSTRUCTIONS ON)
set(USE_AVX_INSTRUCTIONS ON)
set(DLIB_USE_BLAS ON)
set(DLIB_USE_LAPACK ON)
set(DLIB_USE_CUDA OFF)
set(CMAKE_BUILD_TYPE Release)


add_library( lib_opencv SHARED IMPORTED )
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

include(${OPENCV_DIR}/sdk/native/jni/OpenCVConfig.cmake)
include(${DLIB_DIR}/dlib/cmake)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} \
                   -std=c++11  -frtti -Wno-narrowing -Wall -Wno-shift-negative-value \
                   -O2 -ffast-math -Ofast \
                   -mfloat-abi=softfp -mfpu=neon \
                   -fPIE"
)

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} \
                              -Wl,--allow-multiple-definition \
                              -Wl,--whole-archive -fPIE -v")

add_library(
                face_detect SHARED src/main/cpp/jni_face_detect.cpp
                src/main/cpp/dlib_detector.h
                src/main/cpp/utils.cpp
)

target_include_directories(
                        face_detect PRIVATE
                        ${OPENCV_DIR}/sdk/native/jni/include
                        ${DLIB_DIR}
                        ${CMAKE_CURRENT_SOURCE_DIR}
)

target_link_libraries( # Specifies the target library.
                     face_detect
                     android
                     log
                     dlib::dlib
                     lib_opencv
                     jnigraphics
                     m
                     atomic
                     z
)