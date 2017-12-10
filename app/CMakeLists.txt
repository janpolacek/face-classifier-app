# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
add_definitions(-DOS_ANDROID)

set(OPENCV_DIR ../thirdparty/opencv-3.3.1-android-sdk)
add_library( lib_opencv SHARED IMPORTED )
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

include(${OPENCV_DIR}/sdk/native/jni/OpenCVConfig.cmake)


set(DLIB_DIR ../thirdparty/dlib-19.7)
include(${DLIB_DIR}/dlib/cmake)

include_directories(${OPENCV_DIR}/sdk/native/jni/include ${DLIB_DIR})


add_library(
                face_detect SHARED src/main/cpp/jni_face_detect.cpp
                src/main/cpp/detector.h
                src/main/cpp/utils.cpp
)


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
            log-lib

            # Specifies the name of the NDK library that
            # you want CMake to locate.
            log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                     face_detect
                     android
                     dlib::dlib
                     lib_opencv
                     jnigraphics
                     # Links the target library to the log library
                     # included in the NDK.
                     ${log-lib}
)