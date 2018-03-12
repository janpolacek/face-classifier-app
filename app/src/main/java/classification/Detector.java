package classification;

import android.util.Log;

import java.util.Arrays;
import java.util.List;


public class Detector {

    private boolean processing = false;
    private static String TAG = "Detector";

    public Detector() {
    }

    public static Detector create(String modelPath){
        Detector d = new Detector();
        System.loadLibrary("face_detect");
        initDetector(modelPath);
        return d;
    }

    public List<Detection> detectFaces(byte[] nv21Image, int frameWidth, int frameHeight, int frameRotationDegrees, int outputSize) {
        setProcessing(true);
        long startTime = System.currentTimeMillis();

        Detection[] detectionsArray = findFaces(nv21Image, frameWidth, frameHeight, frameRotationDegrees, outputSize);
        List<Detection> detections = Arrays.asList(detectionsArray);

        long estimatedTime = System.currentTimeMillis() - startTime;

        if(detections.size() > 0) {
            Log.d(TAG, "detections:" + detections.size() + ", processing:" + processing + ",time:" + estimatedTime);
        }

        setProcessing(false);
        return detections;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    public static native void initDetector(String modelPath);
    public native Detection[] findFaces(byte[] nv21Image, int frameWidth, int frameHeight, int frameRotationDegrees, int outputSize);
}
