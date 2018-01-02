package jp.faceclass.nn;

import android.util.Log;

import java.util.Arrays;
import java.util.List;


public class DlibFaceDetecor {

    public static boolean isProcessing = false;

    static {
        System.loadLibrary("face_detect");
    }

    public static String TAG = "DlibFaceDetecor";

    public DlibFaceDetecor() {
    }

    public List<Detection> detectFaces(byte[] nv21Image, int frameWidth, int frameHeight, int frameRotationDegrees) {
        isProcessing = true;
        long startTime = System.currentTimeMillis();

        Detection[] detectionsArray = getDetections(nv21Image, frameWidth, frameHeight, frameRotationDegrees);
        List<Detection> detections = Arrays.asList(detectionsArray);

        long estimatedTime = System.currentTimeMillis() - startTime;

        Log.d(TAG, "detections:" + detections.size() + ", processing:" + isProcessing + ",time:" + estimatedTime);
        isProcessing = false;
        return detections;
    }

    public native Detection[] getDetections(byte[] nv21Image, int frameWidth, int frameHeight, int frameRotationDegrees);
}
