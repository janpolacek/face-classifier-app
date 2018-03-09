package jp.faceclass.nn;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.SVM;

public class Classifier {
    private static final String TAG = "Classifier";

    private static final int inputSize = 128;
    private static Mat sample = new Mat(1, inputSize, CvType.CV_32F);
    private SVM svm;

    private Classifier() {}

    public static Classifier create(String modelFilename) {
        Classifier c = new Classifier();
        c.svm = SVM.load(modelFilename);
        return c;
    }

    public float classify(final float[] embeddings) {
        long startTime = System.currentTimeMillis();
        sample.put(0, 0, embeddings);
        float result = svm.predict(sample);
        long estimatedTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "time:" + estimatedTime + ", result: " + result);
        return result;
    }

    public void close() {
    }
}
