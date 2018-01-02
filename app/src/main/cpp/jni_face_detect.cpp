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
  Java_jp_faceclass_nn_DlibFaceDetecor_##METHOD_NAME  // NOLINT

typedef struct _JNI_POSREC {
    jclass cls;
    jmethodID constructortorID;
    jmethodID setLeftId;
    jmethodID setTopId;
    jmethodID setRightId;
    jmethodID setBottomId;
    jmethodID setImageId;
} JNI_POSREC;

JNI_POSREC * jniPosRec = NULL;

struct Detection {
    int left;
    int top;
    int right;
    int bottom;
    cv::Mat image;
};

using DetectorPtr = DLibHOGFaceDetector*;
DetectorPtr detPtr = new DLibHOGFaceDetector("/sdcard/fcd/shape_predictor_68_face_landmarks.dat");

void LoadJniDetectionClass(JNIEnv * env) {
    if (jniPosRec != NULL)
        return;

    jniPosRec = new JNI_POSREC;

    jniPosRec->cls = env->FindClass("jp/faceclass/nn/Detection");

    if(jniPosRec->cls != NULL)
        printf("sucessfully created class");

    jniPosRec->constructortorID = env->GetMethodID(jniPosRec->cls, "<init>", "()V");

    if(jniPosRec->constructortorID != NULL){
        printf("sucessfully created ctorID");
    }

    jniPosRec->setLeftId = env->GetMethodID(jniPosRec->cls, "setLeft", "(I)V");
    jniPosRec->setTopId = env->GetMethodID(jniPosRec->cls, "setTop", "(I)V");
    jniPosRec->setRightId = env->GetMethodID(jniPosRec->cls, "setRight", "(I)V");
    jniPosRec->setBottomId = env->GetMethodID(jniPosRec->cls, "setBottom", "(I)V");
    jniPosRec->setImageId = env->GetMethodID(jniPosRec->cls, "setImage", "([B)V");
}


void fillDetectionImageToJNI(JNIEnv * env, jobject jPosRec, Detection* pDetection){
    size_t data_size = pDetection->image.total() * pDetection->image.elemSize();
    char * data = (char *) pDetection->image.data;

    jbyteArray mJByteArray = env->NewByteArray((jsize) data_size);
    void *dataAlloc = env->GetPrimitiveArrayCritical((jarray)mJByteArray, 0);
    memcpy(dataAlloc, data, data_size);
    env->ReleasePrimitiveArrayCritical(mJByteArray, dataAlloc, 0);

    env->CallVoidMethod(jPosRec, jniPosRec->setImageId, mJByteArray);
}

void FillDetectionValuesToJni(JNIEnv * env, jobject jPosRec, Detection* pDetection) {
    env->CallVoidMethod(jPosRec, jniPosRec->setLeftId, pDetection->left);
    env->CallVoidMethod(jPosRec, jniPosRec->setTopId,  pDetection->top);
    env->CallVoidMethod(jPosRec, jniPosRec->setRightId, pDetection->right);
    env->CallVoidMethod(jPosRec, jniPosRec->setBottomId, pDetection->bottom);
    fillDetectionImageToJNI(env, jPosRec, pDetection);
}


extern "C"
JNIEXPORT jobjectArray JNICALL
FACE_DETECTION_METHOD(getDetections)(JNIEnv *env, jobject instance,
                                                                 jbyteArray nv21Image_,
                                                                 jint frameWidth, jint frameHeight,
                                                                 jint frameRotationDegrees) {
    jbyte *nv21Image = env->GetByteArrayElements(nv21Image_, NULL);
    jniPosRec = NULL;
    LoadJniDetectionClass(env);

    //detection
    cv:: Mat processed = detPtr->processFrame(nv21Image, frameWidth, frameHeight, frameRotationDegrees);
    dlib::cv_image<unsigned char> processed_img(processed);
    detPtr->det(processed_img);
    std::vector<dlib::rectangle> detections = detPtr->getResult();

    jobjectArray jDetArray = env->NewObjectArray((jint)detections.size(), jniPosRec->cls, NULL);

    if(detections.size() <= 0){
        return jDetArray;
    }

    //alignment
    std::vector<dlib::full_object_detection> shapes = detPtr->getShapesFromOriginal();

    dlib::array<dlib::array2d<unsigned char>> face_chips;
    dlib::cv_image<unsigned char> original_image(detPtr->originalImage);

    extract_image_chips(
            original_image,
            dlib::get_face_chip_details(shapes),
            face_chips
    );

    for (int i = 0; i < face_chips.size(); i++) {
        jobject jPosRec = env->NewObject(jniPosRec->cls, jniPosRec->constructortorID);


        Detection *cDet = new Detection();
        cDet->left = (int) detections[i].left() * detPtr->scaleValue;
        cDet->top = (int) detections[i].top() * detPtr->scaleValue;
        cDet->right = (int) detections[i].right() * detPtr->scaleValue;
        cDet->bottom = (int) detections[i].bottom() * detPtr->scaleValue;
        cDet->image = dlib::toMat(face_chips[i]);

        FillDetectionValuesToJni(env, jPosRec, cDet);
        env->SetObjectArrayElement(jDetArray, i, jPosRec);
    }

//    for(int i=0; i<face_chips.size(); i++){
//        dlib::cv_image<unsigned char> aligned_chip(dlib::toMat(face_chips[i]));
//        std::stringstream ss_chip;
//        ss_chip.clear();
//        ss_chip << "/sdcard/detections/" << i << "_chip" << ".png";
//        dlib::save_png(aligned_chip, ss_chip.str());
//    }

    //


    env->ReleaseByteArrayElements(nv21Image_, nv21Image, 0);

    return jDetArray;
}
