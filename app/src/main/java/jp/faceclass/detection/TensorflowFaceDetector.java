package jp.faceclass.detection;

import android.content.res.AssetManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class TensorflowFaceDetector {

    static {
        System.loadLibrary("face_detect");
    }

    public List<Detection> detectFaces(byte[] nv21Image, int frameWidth, int frameHeight, int frameRotationDegrees) {
        Detection detection = getDetectionWithArgs(nv21Image, frameWidth, frameHeight, frameRotationDegrees);
        ArrayList<Detection> detections = new ArrayList<>();

        if(detection != null){
            detections.add(detection);
        }

        return detections;
    }


    public native Detection getDetectionWithArgs(byte[] nv21Image, int frameWidth, int frameHeight, int frameRotationDegrees);
}
