#pragma once

#include "detector.h"

DLibHOGFaceDetector::DLibHOGFaceDetector(const std::string &landmarkmodel)
        : mLandMarkModel(landmarkmodel) {
    init();
    if (!mLandMarkModel.empty() && fileExists(mLandMarkModel)) {
        dlib::deserialize(mLandMarkModel) >> msp;
    }
}
