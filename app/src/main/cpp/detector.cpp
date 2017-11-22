//
// Created by jan on 11/21/17.
//

#include <opencv2/core.hpp>
#include <dlib/opencv/cv_image.h>
#include <opencv2/imgproc/types_c.h>
#include <opencv2/imgproc.hpp>
#include "detector.h"


FaceDetector::FaceDetector() {
    if(isInitialized()){
        return;
    }
    initDetector();
}



bool FaceDetector::isInitialized() {
    return hasFaceDetector;
}

int FaceDetector::detectFaces(const cv::Mat &image) {
    if(!isInitialized()){
        return 0;
    }

    if(image.empty()){
        return 0;
    }


    dlib::cv_image<dlib::bgr_pixel> img(image);

    mDetections = mFaceDetector(img, -1);

    return (int) mDetections.size();
}

cv::Mat FaceDetector::processN21Image(jbyte *nv21Image,
                                     jint frameWidth,
                                     jint frameHeight,
                                     jint frameRotationDegrees) {

    cv::Mat frameMat = cv::Mat(frameHeight, frameWidth, CV_8UC1, (unsigned char*)nv21Image);
    cv::Mat yuvMat = cv::Mat();
    cv::pyrDown(frameMat, yuvMat);

    cv::Mat imgMat = cv::Mat(frameHeight, frameWidth, CV_8UC1);
    cv::cvtColor(yuvMat, imgMat, CV_YUV2BGR_I420);

//    cv::Mat imgMat = cv::Mat(frameHeight, frameWidth, CV_8UC3);
//    cv::cvtColor(yuvMat, imgMat, CV_YUV2BGR_I420);

    if(frameRotationDegrees == 90) {
        rotateMatrix(imgMat, 1);
    } else if(frameRotationDegrees == 180) {
        rotateMatrix(imgMat, 3);
    } else if(frameRotationDegrees == 270 || frameRotationDegrees == -90) {
        rotateMatrix(imgMat, 2);
    }

    return imgMat;
}


void FaceDetector::initDetector() {
    if(hasFaceDetector){
        return;
    }

    mFaceDetector = dlib::get_frontal_face_detector();
    hasFaceDetector = true;
}


void FaceDetector::rotateMatrix(cv::Mat &matImage, int rotFlag) {
    //1=ClockWise
    //2=CounterClockWise
    //3=180degree
    if(rotFlag == 1) {
        cv::transpose(matImage, matImage);
        cv::flip(matImage, matImage, 1);
    } else if(rotFlag == 2) {
        transpose(matImage, matImage);
        cv::flip(matImage, matImage, 0);
    } else if(rotFlag == 3) {
        cv::flip(matImage, matImage, -1);
    }
}


std::vector<dlib::rectangle> FaceDetector::getDetections() {
    return mDetections;
}

int FaceDetector::detectFromUnprocessed(jbyte *nv21Image, jint frameWidth, jint frameHeight,
                                        jint frameRotationDegrees) {

    if(!isInitialized()){
        return 0;
    }

    cv::Mat processed = processN21Image(nv21Image, frameWidth, frameHeight, frameRotationDegrees);
    return detectFaces(processed);
}



