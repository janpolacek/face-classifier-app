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
    std::vector<dlib::full_object_detection> faceShapeMap;

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

    inline cv::Mat transormToRGB(jbyte *nv21Image,
                             jint frameWidth,
                             jint frameHeight){
        cv::Mat yuvMat = cv::Mat(frameHeight + frameHeight / 2, frameWidth, CV_8UC1, (unsigned char *) nv21Image);
        cv::Mat rgbMat = cv::Mat(frameHeight, frameWidth, CV_8UC3);

        cv::cvtColor(yuvMat, rgbMat, CV_YUV2RGB_NV21);
        return rgbMat;
    }

    inline void scaleImage(cv::Mat &mat, int scale){
        cv::resize(mat, mat, cv::Size(mat.cols / scale, mat.rows / scale));
    }

    inline void rotateImage(cv::Mat &mat, int rotation){
        if(rotation == 0){
            return;
        }else if (rotation == 270 || rotation == -90) {
            cv::transpose(mat, mat);
            cv::flip(mat, mat, 1);
        } else if (rotation == 90) {
            cv::transpose(mat, mat);
            cv::flip(mat, mat, 0);
        } else if (rotation == 180) {
            cv::flip(mat, mat, -1);
        }
    }


    inline cv::Mat processFrame(jbyte *nv21Image,
                         jint frameWidth,
                         jint frameHeight,
                         jint frameRotationDegrees) {


        //prelozit frame do normalneho obrazka
        cv::Mat image = transormToRGB(nv21Image, frameWidth, frameHeight);
        //rotovat
        rotateImage(image, frameRotationDegrees);
        originalImage = image.clone();

        //grayscale
        cv::cvtColor(image, image, CV_RGB2GRAY);
        //zmensit
        scaleImage(image, scaleValue);
        return image;
    }

    virtual inline int det(dlib::cv_image<unsigned char> image) {
        mRets = mFaceDetector(image, 1);
        mFaceShapeMap.clear();
        return (int) mRets.size();
    }


    std::vector<dlib::full_object_detection> getFaceShapes(){
        dlib::cv_image<dlib::rgb_pixel> img(originalImage);
        faceShapeMap.clear();

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

    int scaleValue = 4;
    cv::Mat originalImage;

};
