package jp.faceclass.nn;

import android.os.Trace;
import android.util.Log;


import org.tensorflow.Operation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import tensorflow.TensorFlowInferenceInterface;

/** A classifier specialized to label images using TensorFlow. */
public class TensorflowFaceClassifier {
    private static final String TAG = "TensorflowImageClassifier";

    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 3;
    private static final float THRESHOLD = 0.1f;

    // Config values.
    private String inputName;
    private String outputName;
    private int inputSize;

    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private float[] embeddings;
    private String[] outputNames;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    private TensorflowFaceClassifier() {}

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize The input size. A square image of inputSize x inputSize is assumed.
     * @param inputName The label of the image input node.
     * @param outputName The label of the output node.
     * @throws IOException
     */
    public static TensorflowFaceClassifier create(
            String modelFilename,
            String labelFilename,
            int inputSize,
            String inputName,
            String outputName) {
        TensorflowFaceClassifier c = new TensorflowFaceClassifier();
        c.inputName = inputName;
        c.outputName = outputName;

        FileInputStream labelInputStream = null;
        FileInputStream modelInputStream = null;

        try {
            modelInputStream = new FileInputStream(new File(modelFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(labelFilename != null) {

            try {
                labelInputStream = new FileInputStream(new File(labelFilename));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Read the label names into memory.
            Log.i(TAG, "Reading labels from: " + labelFilename);
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(labelInputStream));
                String line;
                while ((line = br.readLine()) != null) {
                    c.labels.add(line);
                }
                br.close();
            } catch (IOException e) {
                throw new RuntimeException("Problem reading label file!" , e);
            }
        }


        c.inferenceInterface = new TensorFlowInferenceInterface(modelInputStream);
        final Operation operation = c.inferenceInterface.graphOperation(outputName);
        final int numOfFeatures = (int) operation.output(0).shape().size(1);

        c.inputSize = inputSize;

        // Pre-allocate buffers.
        c.outputNames = new String[] {outputName};
        c.embeddings = new float[numOfFeatures];

        return c;
    }

    public void classiyImage(final byte[] pixels) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");

        float[] floatValues = new float[pixels.length * 3];

        for (int i = 0; i < pixels.length-1; i++) {
            final int val = pixels[i];
            floatValues[i * 3 + 0] = (val >> 16) & 0xFF;
            floatValues[i * 3 + 1] = (val >> 8) & 0xFF;
            floatValues[i * 3 + 2] = (val) & 0xFF;
        }

        Trace.endSection();

        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
        inferenceInterface.feed("phase_train:0", false);

        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        inferenceInterface.run(outputNames, logStats);
        Trace.endSection();

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        inferenceInterface.fetch(outputName, embeddings);
        Trace.endSection();
    }

    public void close() {
        inferenceInterface.close();
    }
}
