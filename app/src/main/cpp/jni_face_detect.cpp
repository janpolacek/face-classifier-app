#include <jni.h>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/opencv/cv_image.h>
#include <dlib/image_saver/save_png.h>
#include <unordered_map>
#include <sstream>
#include <string>
#include <dlib/opencv.h>

#include "dlib_detector.h"

#define FACE_DETECTION_METHOD(METHOD_NAME) \
  Java_classification_Detector_##METHOD_NAME  // NOLINT


using DetectorPtr = DLibHOGFaceDetector*;
DetectorPtr detPtr = NULL;

//trieda
typedef struct JNI_Detection_Cls {
    jclass cls;
    jmethodID constructortorID;
    jmethodID setLeftId;
    jmethodID setTopId;
    jmethodID setRightId;
    jmethodID setBottomId;
    jmethodID getNativeObjAddrId;
} JNI_Detection_Cls;

//instancia
JNI_Detection_Cls * jniDetClsDef = NULL;

struct Detection {
    int left;
    int top;
    int right;
    int bottom;
    cv::Mat image;
};


void LoadJNIDetectionClass(JNIEnv * env) {
    if (jniDetClsDef != NULL)
        return;
    jniDetClsDef = new JNI_Detection_Cls;

    jniDetClsDef->cls = env->FindClass("classification/Detection");

    if(jniDetClsDef->cls != NULL)
        printf("sucessfully created class");

    jniDetClsDef->constructortorID = env->GetMethodID(jniDetClsDef->cls, "<init>", "()V");

    if(jniDetClsDef->constructortorID != NULL){
        printf("sucessfully created constructor");
    }

    jniDetClsDef->setLeftId = env->GetMethodID(jniDetClsDef->cls, "setLeft", "(I)V");
    jniDetClsDef->setTopId = env->GetMethodID(jniDetClsDef->cls, "setTop", "(I)V");
    jniDetClsDef->setRightId = env->GetMethodID(jniDetClsDef->cls, "setRight", "(I)V");
    jniDetClsDef->setBottomId = env->GetMethodID(jniDetClsDef->cls, "setBottom", "(I)V");
    jniDetClsDef->getNativeObjAddrId = env->GetMethodID(jniDetClsDef->cls, "getNativeObjAddr", "()J");
}

void FillJNIDetectionValues(JNIEnv * env, jobject jPosRec, Detection* pDetection, int outputSize) {
    env->CallVoidMethod(jPosRec, jniDetClsDef->setLeftId, pDetection->left);
    env->CallVoidMethod(jPosRec, jniDetClsDef->setTopId,  pDetection->top);
    env->CallVoidMethod(jPosRec, jniDetClsDef->setRightId, pDetection->right);
    env->CallVoidMethod(jPosRec, jniDetClsDef->setBottomId, pDetection->bottom);
    cv::Mat* mat = (cv::Mat*) env->CallLongMethod(jPosRec, jniDetClsDef->getNativeObjAddrId, 0);
    mat->create(outputSize, outputSize, CV_8UC3);
    memcpy(mat->data, pDetection->image.data, mat->step * mat->rows);
}

extern "C"
JNIEXPORT void JNICALL
FACE_DETECTION_METHOD(initDetector)(JNIEnv *env, jclass type, jstring modelPath_) {
    const char *modelPath = env->GetStringUTFChars(modelPath_, 0);
    const std::string path(modelPath);
    if(detPtr == NULL) {
        detPtr = new DLibHOGFaceDetector(path);
    }

    env->ReleaseStringUTFChars(modelPath_, modelPath);
}


extern "C"
JNIEXPORT jobjectArray JNICALL
FACE_DETECTION_METHOD(findFaces)(JNIEnv *env,
                                 jobject instance,
                                 jbyteArray nv21Image_,
                                 jint frameWidth, jint frameHeight,
                                 jint frameRotationDegrees,
                                 jint outputSize
) {

    jniDetClsDef = NULL;
    LoadJNIDetectionClass(env);

    jbyte *nv21Image = env->GetByteArrayElements(nv21Image_, NULL);

    //detection
    cv:: Mat processed = detPtr->processFrame(nv21Image, frameWidth, frameHeight, frameRotationDegrees);
    dlib::cv_image<unsigned char> processed_img(processed);
    detPtr->det(processed_img);
    std::vector<dlib::rectangle> detections = detPtr->getResult();

    jobjectArray jDetArray = env->NewObjectArray((jint)detections.size(), jniDetClsDef->cls, NULL);

    if(detections.size() <= 0){
        return jDetArray;
    }

    //alignment
    std::vector<dlib::full_object_detection> shapes = detPtr->getShapesFromOriginal();

    dlib::array<dlib::array2d<dlib::rgb_pixel>> face_chips;
    dlib::cv_image<dlib::rgb_pixel> original_image(detPtr->originalImage);

    extract_image_chips(
            original_image,
            dlib::get_face_chip_details(shapes, outputSize, 0.25),
            face_chips
    );
    
    for (int i = 0; i < face_chips.size(); i++) {
        jobject jniDetClsInst = env->NewObject(jniDetClsDef->cls, jniDetClsDef->constructortorID);
        Detection *detection = new Detection();
        detection->left = (int) detections[i].left() * detPtr->scaleValue;
        detection->top = (int) detections[i].top() * detPtr->scaleValue;
        detection->right = (int) detections[i].right() * detPtr->scaleValue;
        detection->bottom = (int) detections[i].bottom() * detPtr->scaleValue;
        detection->image = dlib::toMat(face_chips[i]);

        FillJNIDetectionValues(env, jniDetClsInst, detection, outputSize);
        env->SetObjectArrayElement(jDetArray, i, jniDetClsInst);
    }

//    for(int i=0; i<face_chips.size(); i++){
//        dlib::cv_image<dlib::rgb_pixel> aligned_chip(dlib::toMat(face_chips[i]));
//        dlib::save_png(aligned_chip, "/sdcard/classifier/detections/chip_" + std::to_string(i) + ".png");
//    }
//
//    dlib::cv_image<dlib::rgb_pixel> aligned_chip(detPtr->originalImage);
//    dlib::save_png(aligned_chip, "/sdcard/classifier/detections/original.png");

    env->ReleaseByteArrayElements(nv21Image_, nv21Image, 0);
    return jDetArray;
}
