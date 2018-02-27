package jp.faceclass.nn;

import android.os.Trace;
import android.util.Log;

import org.tensorflow.Operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import tensorflow.TensorFlowInferenceInterface;

public class Classifier {
    private static final String TAG = "EmbeddingsExtractor";

    private static final int inputSize = 128;
    private static final String inputName = "embeddings";
    private static final String outputName = "embeddings";

    // Pre-allocated buffers.
    private float[] output;
    private String[] outputNames;
    private int outputSize;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    private Classifier() {}


    public static Classifier create(String modelFilename) {
        Classifier c = new Classifier();
        FileInputStream modelInputStream = null;

        try {
            modelInputStream = new FileInputStream(new File(modelFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        c.inferenceInterface = new TensorFlowInferenceInterface(modelInputStream);
        final Operation operation = c.inferenceInterface.graphOperation(outputName);
        c.outputSize = (int) operation.output(0).shape().size(1);

        // Pre-allocate buffers.
        c.outputNames = new String[] {outputName};
        c.output = new float[c.outputSize];

        return c;
    }

    public float [] runModel(final float[] embeddings) {
        long startTime = System.currentTimeMillis();
        float [] data = embeddings;
        feedInference(data);
        runInference();
        float [] result = fetchResult();

        long estimatedTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "runModel time:" + estimatedTime);

        return result;
    }

    private void feedInference(float[] floatValues){
        Trace.beginSection("feedInference");

        inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
        inferenceInterface.feed("phase_train:0", false);

        Trace.endSection();
    }

    private void runInference() {
        // Run the inference call.
        Trace.beginSection("runInference");
        inferenceInterface.run(outputNames, logStats);
        Trace.endSection();
    }

    private float [] fetchResult(){
        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetchResult");
        inferenceInterface.fetch(outputName, output);
        Trace.endSection();
        return output;
    }


    public void close() {
        inferenceInterface.close();
    }

    public static int getInputSize() {
        return inputSize;
    }
}