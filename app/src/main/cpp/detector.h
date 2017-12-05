#pragma once

#include <dlib/image_loader/load_image.h>
#include <dlib/image_processing.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/opencv/cv_image.h>
#include <dlib/image_loader/load_image.h>
#include <jni.h>
#include <memory>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/opencv.hpp>
#include <stdio.h>
#include <string>
#include <vector>
#include <unordered_map>


class DLibHOGFaceDetector {
private:
    typedef dlib::scan_fhog_pyramid<dlib::pyramid_down<6>> image_scanner_type;
    dlib::object_detector<image_scanner_type> mObjectDetector;
    std::string mLandMarkModel;
    dlib::shape_predictor msp;
    std::unordered_map<int, dlib::full_object_detection> mFaceShapeMap;
    dlib::frontal_face_detector mFaceDetector;
    int frameCount = 0;

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

    bool fileExists(std::string name) {
        std::ifstream ifs(name);
        return ifs.good();
    }

    virtual inline int det(const std::string &path) {
        if (!fileExists(mModelPath) || !fileExists(path)) {
            return 0;
        }
        cv::Mat src_img = cv::imread(path, CV_LOAD_IMAGE_COLOR);
        if (src_img.empty())
            return 0;
        int img_width = src_img.cols;
        int img_height = src_img.rows;
        int im_size_min = MIN(img_width, img_height);
        int im_size_max = MAX(img_width, img_height);

        float scale = float(INPUT_IMG_MIN_SIZE) / float(im_size_min);
        if (scale * im_size_max > INPUT_IMG_MAX_SIZE) {
            scale = (float) INPUT_IMG_MAX_SIZE / (float) im_size_max;
        }

        if (scale != 1.0) {
            cv::Mat outputMat;
            cv::resize(src_img, outputMat,
                       cv::Size((int) (img_width * scale), (int) (img_height * scale)));
            src_img = outputMat;
        }

        dlib::cv_image<unsigned char> cimg(src_img);

        double thresh = 0.5;
        mRets = mObjectDetector(cimg, thresh);
        mFaceShapeMap.clear();
        // Process shape
        if (mRets.size() != 0 && !mLandMarkModel.empty()) {
            for (unsigned long j = 0; j < mRets.size(); ++j) {
                dlib::full_object_detection shape = msp(cimg, mRets[j]);
                mFaceShapeMap[j] = shape;
            }
        }
        return (int) mRets.size();
    }

    void rotateMatrix(cv::Mat &matImage, int rotFlag) {
        //1=ClockWise
        //2=CounterClockWise
        //3=180degree
        if (rotFlag == 1) {
            cv::transpose(matImage, matImage);
            cv::flip(matImage, matImage, 1);
        } else if (rotFlag == 2) {
            transpose(matImage, matImage);
            cv::flip(matImage, matImage, 0);
        } else if (rotFlag == 3) {
            cv::flip(matImage, matImage, -1);
        }
    }

    bool saveToFile(cv::Mat mat, int frameCount, int step) {
        return false;
        std::stringstream ss;
        ss.clear();
        ss << "/sdcard/detections/" << frameCount << "-" << step << ".png";
        dlib::cv_image<unsigned char> img(mat);
        dlib::save_png(img, ss.str());
        return false;
    }

    cv::Mat processFrame(jbyte *nv21Image,
                         jint frameWidth,
                         jint frameHeight,
                         jint frameRotationDegrees) {

        cv::Mat yuvMat = cv::Mat(frameHeight + frameHeight / 2, frameWidth, CV_8UC1,
                                 (unsigned char *) nv21Image);
        cv::Mat bgrMat = cv::Mat(frameHeight, frameWidth, CV_8UC1);

        cv::cvtColor(yuvMat, bgrMat, CV_YUV2GRAY_NV21);
        saveToFile(bgrMat, frameCount, 3);

        cv::Mat scaledMat = cv::Mat();
        cv::resize(bgrMat, scaledMat, cv::Size(bgrMat.cols / scaleValue, bgrMat.rows / scaleValue));
        saveToFile(scaledMat, frameCount, 4);


        if (frameRotationDegrees == 90) {
            rotateMatrix(scaledMat, 2);
        } else if (frameRotationDegrees == 180) {
            rotateMatrix(scaledMat, 3);
        } else if (frameRotationDegrees == 270 || frameRotationDegrees == -90) {
            rotateMatrix(scaledMat, 1);
        }

        saveToFile(scaledMat, frameCount, 5);

        return scaledMat;
    }

    virtual inline int det(dlib::cv_image<unsigned char> image) {
        mRets = mFaceDetector(image, 0);
        mFaceShapeMap.clear();
        // Process shape
        if (mRets.size() != 0 && !mLandMarkModel.empty()) {
            for (unsigned long j = 0; j < mRets.size(); ++j) {
                dlib::full_object_detection shape = msp(image, mRets[j]);
                mFaceShapeMap[j] = shape;
            }
        }
        return (int) mRets.size();
    }

    std::unordered_map<int, dlib::full_object_detection> &getFaceShapeMap() {
        return mFaceShapeMap;
    }

    virtual inline std::vector<dlib::rectangle> getResult() { return mRets; }

    virtual ~DLibHOGFaceDetector() {}

    int scaleValue = 6;
protected:
    std::vector<dlib::rectangle> mRets;
    std::string mModelPath;
    const int INPUT_IMG_MAX_SIZE = 800;
    const int INPUT_IMG_MIN_SIZE = 600;
};


bool dirExists(const char *name) {
    struct stat file_info;
    if (stat(name, &file_info) != 0)
        return false;
    return (file_info.st_mode & S_IFDIR) != 0;
}


bool saveToFile(dlib::cv_image<unsigned char> img, int frameCount, int step) {
    return false;
    std::stringstream ss;
    ss.clear();
    ss << "/sdcard/detections/" << frameCount << "-" << step << ".png";
    dlib::save_png(img, ss.str());
    return false;
}

