#include <jni.h>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/opencv/cv_image.h>


using namespace std;
using namespace dlib;
using namespace cv;

void rotateMat(Mat &matImage, int rotFlag) {
    //1=ClockWise
    //2=CounterClockWise
    //3=180degree
    if(rotFlag == 1) {transpose(matImage, matImage);flip(matImage, matImage, 1);}
    else if(rotFlag == 2) {transpose(matImage, matImage);flip(matImage, matImage, 0);}
    else if(rotFlag == 3) {flip(matImage, matImage, -1);}
}

extern "C"
JNIEXPORT jobject JNICALL
Java_jp_faceclass_detection_TensorflowFaceDetector_getDetectionWithArgs(JNIEnv *env,
                                                                        jobject instance,
                                                                        jbyteArray nv21Image_,
                                                                        jint frameWidth,
                                                                        jint frameHeight,
                                                                        jint frameRotationDegrees) {
    jbyte *nv21Image = env->GetByteArrayElements(nv21Image_, NULL);

    Mat yuvMat = Mat(frameHeight+frameHeight/2, frameWidth, CV_8UC1, (unsigned char*)nv21Image);
    Mat bgrMat = Mat(frameHeight, frameWidth, CV_8UC3);
    cvtColor(yuvMat, bgrMat, CV_YUV2BGRA_NV21);
    cvtColor(yuvMat, bgrMat, CV_YUV2GRAY_NV21);

    if(frameRotationDegrees == 90) {rotateMat(bgrMat, 1);}
    else if(frameRotationDegrees == 180) {rotateMat(bgrMat, 3);}
    else if(frameRotationDegrees == 270 || frameRotationDegrees == -90) {rotateMat(bgrMat, 2);}

    frontal_face_detector detector = get_frontal_face_detector();
    shape_predictor pose_model;
//    todo: path to landmarks
    deserialize("../assets/shape_predictor_68_face_landmarks.dat") >> pose_model;
    cv_image<bgr_pixel> cimg(bgrMat);
    std::vector<dlib::rectangle> faces = detector(cimg, 0.5);
    std::vector<dlib::full_object_detection> shapes;

    for (unsigned long i = 0; i < faces.size(); ++i){
        shapes.push_back(pose_model(cimg, faces[i]));
    }

    long t = shapes.size();


    jclass detectionClass = env->FindClass("jp/faceclass/detection/Detection");

    if(detectionClass == NULL){
        return NULL;
    }
    jmethodID detectionClassConstructor = env->GetMethodID(detectionClass, "<init>", "(IIII)V");

    if(detectionClassConstructor == NULL){
        return NULL;
    }

    jobject detectionObject = env->NewObject(detectionClass, detectionClassConstructor, t, t, t, t);

    if(detectionObject == NULL){
        return NULL;
    }


    env->ReleaseByteArrayElements(nv21Image_, nv21Image, 0);
    return detectionObject;
}


