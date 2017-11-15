#include <jni.h>
#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>

#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/opencv/cv_image.h>
//#include <android/log.h>


//#define LOGV(TAG,...) __android_log_print(ANDROID_LOG_VERBOSE, TAG,__VA_ARGS__)
//#define LOGD(TAG,...) __android_log_print(ANDROID_LOG_DEBUG  , TAG,__VA_ARGS__)
//#define LOGI(TAG,...) __android_log_print(ANDROID_LOG_INFO   , TAG,__VA_ARGS__)
//#define LOGW(TAG,...) __android_log_print(ANDROID_LOG_WARN   , TAG,__VA_ARGS__)
//#define LOGE(TAG,...) __android_log_print(ANDROID_LOG_ERROR  , TAG,__VA_ARGS__)

const char* TAG = "face_detect_JNI";


class FaceDetector{
private:
    std::string mLandMarkModel;
    dlib::frontal_face_detector mFaceDetector;
    dlib::shape_predictor msp;
    std::vector<dlib::rectangle> mRets;

    bool initialized = false;

    void init(){
        mFaceDetector = dlib::get_frontal_face_detector();
//        LOGV(TAG, "%s", "init() ");
//        LOGV(TAG, "%s", mLandMarkModel.c_str());
        if (!mLandMarkModel.empty() && fileExists(mLandMarkModel.c_str())) {
            dlib::deserialize(mLandMarkModel) >> msp;
            initialized = true;
        }
    }

    void rotateMat(cv::Mat &matImage, int rotFlag) {
        //1=ClockWise
        //2=CounterClockWise
        //3=180degree
        if(rotFlag == 1) {
            transpose(matImage, matImage);
            flip(matImage, matImage, 1);
        } else if(rotFlag == 2) {
            transpose(matImage, matImage);
            flip(matImage, matImage, 0);
        } else if(rotFlag == 3) {
            flip(matImage, matImage, -1);
        }
    }

    bool fileExists(const char *fileName) {
        std::ifstream infile(fileName);
        return infile.good();
    }


public:
    FaceDetector(std::string landMarkModel){
        mLandMarkModel = landMarkModel;
        init();
    }

    bool isInitialized(){
        return initialized;
    }


    int detect(const cv::Mat& image){
//        LOGV(TAG, "%s", "detect() ", "%s", "detection start");

        if(!initialized){
//            LOGV(TAG, "%s", "detect() ", "%s", "not initialized");
            return 0;
        }

        if (image.empty()){
//            LOGV(TAG, "%s", "detect() ", "%s", "empty image");
            return 0;
        }

        dlib::cv_image<dlib::bgr_pixel> img(image);
        mRets = mFaceDetector(img, 0.5);
//        LOGV(TAG, "%s", "detect() ", "%s", "detected: ", "%d", mRets.size());

        return (int) mRets.size();
    }


//    cv::Mat processN21Image(
//            jbyte *nv21Image,
//            jint frameWidth,
//            jint frameHeight,
//            jint frameRotationDegrees
//
//    ) {
//
//        cv::Mat yuvMat = cv::Mat(frameHeight+frameHeight/2, frameWidth, CV_8UC1, (unsigned char*)nv21Image);
//        cv::Mat bgrMat = cv::Mat(frameHeight, frameWidth, CV_8UC3);
//        cvtColor(yuvMat, bgrMat, CV_YUV2BGRA_NV21);
//        cvtColor(yuvMat, bgrMat, CV_YUV2GRAY_NV21);
//
//        if(frameRotationDegrees == 90) {
//            rotateMat(bgrMat, 1);
//        } else if(frameRotationDegrees == 180) {
//            rotateMat(bgrMat, 3);
//        } else if(frameRotationDegrees == 270 || frameRotationDegrees == -90) {
//            rotateMat(bgrMat, 2);
//        }
//    }

};

void rotateMat(cv::Mat &matImage, int rotFlag) {
    //1=ClockWise
    //2=CounterClockWise
    //3=180degree
    if(rotFlag == 1) {
        cv::transpose(matImage, matImage);
        cv::flip(matImage, matImage, 1);
    } else if(rotFlag == 2) {
        cv::transpose(matImage, matImage);
        cv::flip(matImage, matImage, 0);
    } else if(rotFlag == 3) {
        cv::flip(matImage, matImage, -1);
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_jp_faceclass_detection_TensorflowFaceDetector_getDetectionWithArgs(JNIEnv *env,
                                                                        jobject instance,
                                                                        jbyteArray nv21Image_,
                                                                        jint frameWidth,
                                                                        jint frameHeight,
                                                                        jint frameRotationDegrees) {
    jbyte *nv21Image = env->GetByteArrayElements(nv21Image_, NULL);
//    TODO: INIT ONCE ONLY
//    FaceDetector faceDetector = FaceDetector("/sdcard/fcd/shape_predictor_68_face_landmarks.dat");
//    cv::Mat processed = faceDetector.processN21Image(
//            nv21Image,
//            frameWidth,
//            frameHeight,
//            frameRotationDegrees
//    );
//    int t = faceDetector.detect(processed);

    int t = 0;

    jclass detectionClass = env->FindClass("jp/faceclass/detection/Detection");

    if(detectionClass == NULL){
        return NULL;
    }
    jmethodID detectionClassConstructor = env->GetMethodID(detectionClass, "<init>", "(IIII)V");

    if(detectionClassConstructor == NULL){
        return NULL;
    }

    jobject detectionObject = env->NewObject(detectionClass, detectionClassConstructor, t, t, t, t);

    if(detectionObject == NULL){
        return NULL;
    }


    env->ReleaseByteArrayElements(nv21Image_, nv21Image, 0);
    return detectionObject;
}