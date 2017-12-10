#include <jni.h>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/opencv/cv_image.h>
#include <dlib/image_saver/save_png.h>
#include "detector.h"


typedef struct _JNI_POSREC {
    jclass cls;
    jmethodID constructortorID;
    jfieldID leftID;
    jfieldID topID;
    jfieldID rightID;
    jfieldID bottomID;
} JNI_POSREC;

JNI_POSREC * jniPosRec = NULL;

struct Detection {
    int left;
    int top;
    int right;
    int bottom;
};

using DetectorPtr = DLibHOGFaceDetector*;
DetectorPtr detPtr = new DLibHOGFaceDetector("/sdcard/fcd/shape_predictor_68_face_landmarks.dat");

void LoadJniDetectionClass(JNIEnv * env) {

    if (jniPosRec != NULL)
        return;

    jniPosRec = new JNI_POSREC;

    jniPosRec->cls = env->FindClass("jp/faceclass/detection/Detection");

    if(jniPosRec->cls != NULL)
        printf("sucessfully created class");

    jniPosRec->constructortorID = env->GetMethodID(jniPosRec->cls, "<init>", "()V");
    if(jniPosRec->constructortorID != NULL){
        printf("sucessfully created ctorID");
    }

    jniPosRec->leftID = env->GetFieldID(jniPosRec->cls, "left", "I");
    jniPosRec->topID = env->GetFieldID(jniPosRec->cls, "top", "I");
    jniPosRec->rightID = env->GetFieldID(jniPosRec->cls, "right", "I");
    jniPosRec->bottomID = env->GetFieldID(jniPosRec->cls, "bottom", "I");
}


void FillDetectionValuesToJni(JNIEnv * env, jobject jPosRec, Detection* pDetection) {
    jint left = (jint) pDetection->left;
    env->SetIntField(jPosRec, jniPosRec->leftID, left);

    jint top = (jint) pDetection->top;
    env->SetIntField(jPosRec, jniPosRec->topID, top);

    jint right = (jint) pDetection->right;
    env->SetIntField(jPosRec, jniPosRec->rightID, right);

    jint bottom = (jint) pDetection->bottom;
    env->SetIntField(jPosRec, jniPosRec->bottomID, bottom);
}


extern "C"
JNIEXPORT jobjectArray JNICALL
Java_jp_faceclass_detection_DlibFaceDetecor_getDetections(JNIEnv *env, jobject instance,
                                                                 jbyteArray nv21Image_,
                                                                 jint frameWidth, jint frameHeight,
                                                                 jint frameRotationDegrees) {
    jbyte *nv21Image = env->GetByteArrayElements(nv21Image_, NULL);

    jniPosRec = NULL;
    LoadJniDetectionClass(env);

    cv:: Mat processed = detPtr->processFrame(nv21Image, frameWidth, frameHeight, frameRotationDegrees);
    dlib::cv_image<unsigned char> img(processed);
    detPtr->det(img);
    std::vector<dlib::rectangle> detections = detPtr->getResult();

    jobjectArray jDetArray = env->NewObjectArray((jint)detections.size(), jniPosRec->cls, NULL);

    if(detections.size() <= 0){
        return jDetArray;
    }

    for (size_t i = 0; i < detections.size(); i++) {
        jobject jPosRec = env->NewObject(jniPosRec->cls, jniPosRec->constructortorID);

        Detection *cDet = new Detection();
        cDet->left = (int) detections[i].left() * detPtr->scaleValue;
        cDet->top = (int) detections[i].top() * detPtr->scaleValue;
        cDet->right = (int) detections[i].right() * detPtr->scaleValue;
        cDet->bottom = (int) detections[i].bottom() * detPtr->scaleValue;

        FillDetectionValuesToJni(env, jPosRec, cDet);
        env->SetObjectArrayElement(jDetArray, i, jPosRec);
    }

    env->ReleaseByteArrayElements(nv21Image_, nv21Image, 0);

    return jDetArray;
}
