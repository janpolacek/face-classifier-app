//
// Created by jan on 11/21/17.
//

#include <opencv2/core.hpp>
#include <dlib/opencv/cv_image.h>
#include <opencv2/imgproc/types_c.h>
#include <opencv2/imgproc.hpp>
#include "detector.h"
#include <opencv2/opencv.hpp>
#include <opencv2/imgcodecs.hpp>
#include <dlib/image_saver/save_png.h>

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

    dlib::cv_image<unsigned char> img(image);

    mDetections = mFaceDetector(img, 0);

    for(int i=0; i < mDetections.size(); i++){
        dlib::draw_rectangle(img, mDetections[i], dlib::rgb_pixel(255, 0, 0), 1);
    }

    if(mDetections.size() > 0){
        saveToFile(img, frameCount, 6);
    }


    return (int) mDetections.size();
}

cv::Mat FaceDetector::processN21Image(jbyte *nv21Image,
                                     jint frameWidth,
                                     jint frameHeight,
                                     jint frameRotationDegrees) {

    cv::Mat yuvMat = cv::Mat(frameHeight + frameHeight/2, frameWidth, CV_8UC1, (unsigned char*)nv21Image);
    cv::Mat bgrMat = cv::Mat(frameHeight, frameWidth, CV_8UC1);

    cv::cvtColor(yuvMat, bgrMat, CV_YUV2GRAY_NV21);
    saveToFile(bgrMat, frameCount, 3);

    cv::Mat scaledMat = cv::Mat();
    cv::resize(bgrMat, scaledMat, cv::Size(bgrMat.cols/scaleValue, bgrMat.rows/scaleValue));
    saveToFile(scaledMat, frameCount, 4);


    if(frameRotationDegrees == 90) {
        rotateMatrix(scaledMat, 2);
    } else if(frameRotationDegrees == 180) {
        rotateMatrix(scaledMat, 3);
    } else if(frameRotationDegrees == 270 || frameRotationDegrees == -90) {
        rotateMatrix(scaledMat, 1);
    }

    saveToFile(scaledMat, frameCount, 5);

    return scaledMat;
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

    frameCount++;

    cv::Mat processed = processN21Image(nv21Image, frameWidth, frameHeight, frameRotationDegrees);
    return detectFaces(processed);
}

bool FaceDetector::saveToFile(cv::Mat mat, int frameCount, int step) {
    return false;
    std::stringstream ss;
    ss.clear();
    ss << "/sdcard/detections/" << frameCount << "-" << step <<  ".png";
    dlib::cv_image<unsigned char> img(mat);
    dlib::save_png(img, ss.str());
    return false;
}

bool FaceDetector::saveToFile(dlib::cv_image<unsigned char> img, int frameCount, int step) {
    return false;
    std::stringstream ss;
    ss.clear();
    ss << "/sdcard/detections/" << frameCount << "-" << step <<  ".png";
    dlib::save_png(img, ss.str());
    return false;
}



