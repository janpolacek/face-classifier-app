package jp.faceclass.nn;

import android.os.Environment;
import android.util.Log;

import java.util.Arrays;
import java.util.List;


public class DlibFaceDetecor {

    public static boolean isProcessing = false;
    public static String TAG = "DlibFaceDetecor";

    public DlibFaceDetecor() {
    }

    public static DlibFaceDetecor create(String modelPath){
        DlibFaceDetecor d = new DlibFaceDetecor();
        System.loadLibrary("face_detect");
        initDetector(modelPath);
        return d;
    }

    public List<Detection> detectFaces(byte[] nv21Image, int frameWidth, int frameHeight, int frameRotationDegrees) {
        isProcessing = true;
        long startTime = System.currentTimeMillis();

        Detection[] detectionsArray = findFaces(nv21Image, frameWidth, frameHeight, frameRotationDegrees);
        List<Detection> detections = Arrays.asList(detectionsArray);

        long estimatedTime = System.currentTimeMillis() - startTime;

        Log.d(TAG, "detections:" + detections.size() + ", processing:" + isProcessing + ",time:" + estimatedTime);
        isProcessing = false;
        return detections;
    }

    public static native void initDetector(String modelPath);
    public native Detection[] findFaces(byte[] nv21Image, int frameWidth, int frameHeight, int frameRotationDegrees);
}
