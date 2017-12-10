#pragma once

#include <dlib/image_processing.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/opencv/cv_image.h>
#include <jni.h>
#include <memory>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/opencv.hpp>
#include <stdio.h>
#include <string>
#include <vector>
#include <unordered_map>

#include "utils.cpp"

class DLibHOGFaceDetector {
private:
    std::string mLandMarkModel;
    dlib::shape_predictor msp;
    std::unordered_map<int, dlib::full_object_detection> mFaceShapeMap;
    dlib::frontal_face_detector mFaceDetector;
    std::vector<dlib::rectangle> mRets;

    inline void init() {
        mFaceDetector = dlib::get_frontal_face_detector();
    }

public:
    DLibHOGFaceDetector(const std::string &landmarkmodel)
            : mLandMarkModel(landmarkmodel) {
        init();
        if (!mLandMarkModel.empty() && fileExists(mLandMarkModel)) {
            dlib::deserialize(mLandMarkModel) >> msp;
        }
    }


    inline cv::Mat processFrame(jbyte *nv21Image,
                         jint frameWidth,
                         jint frameHeight,
                         jint frameRotationDegrees) {

        cv::Mat yuvMat = cv::Mat(frameHeight + frameHeight / 2, frameWidth, CV_8UC1, (unsigned char *) nv21Image);
        cv::Mat grayMat = cv::Mat(frameHeight, frameWidth, CV_8UC1);

        cv::cvtColor(yuvMat, grayMat, CV_YUV2GRAY_NV21);

        cv::Mat scaledMat = cv::Mat();
        cv::resize(grayMat, scaledMat, cv::Size(grayMat.cols / scaleValue, grayMat.rows / scaleValue));

        if(frameRotationDegrees != 0){
            rotateMatrix(scaledMat, frameRotationDegrees);
        }


        return scaledMat;
    }

    virtual inline int det(dlib::cv_image<unsigned char> image) {
        mRets = mFaceDetector(image, 0);
        mFaceShapeMap.clear();
        // Process shape
//        if (mRets.size() != 0 && !mLandMarkModel.empty()) {
//            for (unsigned long j = 0; j < mRets.size(); ++j) {
//                dlib::full_object_detection shape = msp(image, mRets[j]);
//                mFaceShapeMap[j] = shape;
//            }
//        }
        return (int) mRets.size();
    }

    inline int extractChipsFromOriginal(int scale){

    }

    std::unordered_map<int, dlib::full_object_detection> &getFaceShapeMap() {
        return mFaceShapeMap;
    }

    virtual inline std::vector<dlib::rectangle> getResult() { return mRets; }

    virtual ~DLibHOGFaceDetector() {}

    int scaleValue = 8;
};
