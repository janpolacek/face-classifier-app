//
// Created by jan on 11/21/17.
//

#ifndef FACE_CLASSIFIER_APP_DETECTOR_H
#define FACE_CLASSIFIER_APP_DETECTOR_H


#include <string>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing/shape_predictor.h>
#include <opencv2/core/mat.hpp>
#include <jni.h>

class FaceDetector{
private:
//    std::string mLandMarkModel;
    dlib::frontal_face_detector mFaceDetector;
//    dlib::shape_predictor mShapePredictor;
    std::vector<dlib::rectangle> mDetections;


//    bool hasLandMarkModel = false;
    bool hasFaceDetector = false;

    void initDetector();
//    void loadLandMarkModel();
    void rotateMatrix(cv::Mat &matImage, int rotFlag);
//    bool checkFileExists(const char *fileName);

public:
    FaceDetector();
//    FaceDetector(std::string landMarkModel);
    bool isInitialized();
    int scaleValue = 6;
    int frameCount = 0;
    int detectFaces(const cv::Mat& image);
    bool saveToFile(cv::Mat mat, int frameCount, int step);
    bool saveToFile(dlib:: cv_image<unsigned char> img, int frameCount, int step);

    cv::Mat processN21Image(
        jbyte *nv21Image,
        jint frameWidth,
        jint frameHeight,
        jint frameRotationDegrees
    );

    int detectFromUnprocessed(
            jbyte *nv21Image,
            jint frameWidth,
            jint frameHeight,
            jint frameRotationDegrees
    );

    std::vector<dlib::rectangle> getDetections();

};


#endif //FACE_CLASSIFIER_APP_DETECTOR_H
