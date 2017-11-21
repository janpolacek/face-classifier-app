#include <jni.h>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/opencv/cv_image.h>
#include "detector.h"

static FaceDetector faceDetector = FaceDetector("/sdcard/fcd/shape_predictor_68_face_landmarks.dat");;

extern "C"
JNIEXPORT jobject JNICALL
Java_jp_faceclass_detection_TensorflowFaceDetector_getDetectionWithArgs(JNIEnv *env,
                                                                        jobject instance,
                                                                        jbyteArray nv21Image_,
                                                                        jint frameWidth,
                                                                        jint frameHeight,
                                                                        jint frameRotationDegrees) {
    jbyte *nv21Image = env->GetByteArrayElements(nv21Image_, NULL);

    int numberOfDetections = faceDetector.detectFromUnprocessed(
            nv21Image,
            frameWidth,
            frameHeight,
            frameRotationDegrees
    );


    jclass detectionClass = env->FindClass("jp/faceclass/detection/Detection");

    if(detectionClass == NULL){
        return NULL;
    }
    jmethodID detectionClassConstructor = env->GetMethodID(detectionClass, "<init>", "(IIII)V");

    if(detectionClassConstructor == NULL){
        return NULL;
    }

    jobject detectionObject = env->NewObject(detectionClass, detectionClassConstructor, numberOfDetections, numberOfDetections, numberOfDetections, numberOfDetections);

    if(detectionObject == NULL){
        return NULL;
    }


    env->ReleaseByteArrayElements(nv21Image_, nv21Image, 0);
    return detectionObject;
}
