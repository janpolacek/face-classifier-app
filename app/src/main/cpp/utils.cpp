#include <dlib/opencv/cv_image.h>

inline bool fileExists(std::string name) {
    std::ifstream ifs(name);
    return ifs.good();
}
