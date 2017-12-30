#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <dlib/opencv/cv_image.h>

inline void rotateMatrix(cv::Mat &matImage, int rotation) {

    if (rotation == 270 || rotation == -90) {
        cv::transpose(matImage, matImage);
        cv::flip(matImage, matImage, 1);
    } else if (rotation == 90) {
        transpose(matImage, matImage);
        cv::flip(matImage, matImage, 0);
    } else if (rotation == 180) {
        cv::flip(matImage, matImage, -1);
    }
}


inline bool fileExists(std::string name) {
    std::ifstream ifs(name);
    return ifs.good();
}
