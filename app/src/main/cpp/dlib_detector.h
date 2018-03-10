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

    inline cv::Mat getRGBMat(jbyte *nv21Image,
                             jint frameWidth,
                             jint frameHeight){
        cv::Mat yuvMat = cv::Mat(frameHeight + frameHeight / 2, frameWidth, CV_8UC1, (unsigned char *) nv21Image);
        cv::Mat grayMat = cv::Mat(frameHeight, frameWidth, CV_8UC3);

        cv::cvtColor(yuvMat, grayMat, CV_YUV2RGB_NV21);
        return grayMat;
    }

    inline cv::Mat scaleImage(cv::Mat mat, int scale){
        cv::Mat scaledMat = cv::Mat();
        cv::resize(mat, scaledMat, cv::Size(mat.cols / scale, mat.rows / scale));
        return scaledMat;
    }

    inline cv::Mat rotateImage(cv::Mat mat, int rotation){
        if(rotation != 0){
            rotateMatrix(mat, rotation);
        }

        return mat;
    }


    inline cv::Mat processFrame(jbyte *nv21Image,
                         jint frameWidth,
                         jint frameHeight,
                         jint frameRotationDegrees) {


        //prelozit frame do normalneho obrazka
        cv::Mat rgbMat = getRGBMat(nv21Image, frameWidth, frameHeight);
        //rotovat
        cv::Mat rotated = rotateImage(rgbMat, frameRotationDegrees);
        originalImage = rotated.clone();

        //grayscale
        cv::Mat grayMat(rotated.size(), CV_8UC1);
        cv::cvtColor(rotated, grayMat, CV_RGB2GRAY);

        //zmensit
        cv::Mat scaledMat = scaleImage(grayMat, scaleValue);

        return scaledMat;
    }

    virtual inline int det(dlib::cv_image<unsigned char> image) {
        mRets = mFaceDetector(image, 0);
        mFaceShapeMap.clear();
        return (int) mRets.size();
    }


    std::vector<dlib::full_object_detection> getShapesFromOriginal(){
        std::vector<dlib::full_object_detection> faceShapeMap;
        dlib::cv_image<dlib::rgb_pixel> img(originalImage);

        if (mRets.size() != 0 && !mLandMarkModel.empty()) {
            for (unsigned long j = 0; j < mRets.size(); ++j) {
                dlib::rectangle scaledUpRectangle = dlib::rectangle();

                scaledUpRectangle.set_left(mRets[j].left()*scaleValue);
                scaledUpRectangle.set_top(mRets[j].top()*scaleValue);
                scaledUpRectangle.set_right(mRets[j].right()*scaleValue);
                scaledUpRectangle.set_bottom(mRets[j].bottom()*scaleValue);

                dlib::full_object_detection shape = msp(img, scaledUpRectangle);
                faceShapeMap.push_back(shape);
            }
        }

        return faceShapeMap;
    }


    virtual inline std::vector<dlib::rectangle> getResult() { return mRets; }

    virtual ~DLibHOGFaceDetector() {}

    int scaleValue = 5;
    cv::Mat originalImage;

};
