package classification;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Trace;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import tensorflow.TensorFlowInferenceInterface;

import static android.graphics.Bitmap.Config.RGB_565;

public class Extractor {
    private static final String TAG = "Extractor";

    private boolean processing = false;
    private static final int inputSize = 160;
    private static final String inputName = "input:0";
    private static final String outputName = "embeddings";

    // Pre-allocated buffers.
    private float[] output;
    private String[] outputNames;
    private int outputSize;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    private Extractor() {}


    public static Extractor create(String modelFilename) {

        Extractor c = new Extractor();
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

    public float [] extractEmbeddings(Mat mat) {
        setProcessing(true);
        long startTime = System.currentTimeMillis();
        getMultiChannelArray(mat);
//        feedInference(mat.get(0, 0));
//        runInference();
//        float [] result = fetchResult();
//
//        long estimatedTime = System.currentTimeMillis() - startTime;
//        Log.d(TAG, "extraction time:" + estimatedTime);
//
//        setProcessing(false);
//        return result;
        return null;
    }

    private float [] processGrayscaleData(byte[] data){
        Trace.beginSection("processData");
        float[] floatValues = new float[data.length*3];

        for (int i = 0; i < data.length-1; i++) {
            final int val = data[i];
            floatValues[i * 3 + 0] = (val >> 16) & 0xFF;
            floatValues[i * 3 + 1] = (val >> 8) & 0xFF;
            floatValues[i * 3 + 2] = (val) & 0xFF;
        }

        Trace.endSection();
        return floatValues;
    }

    private float [] processRGBData(byte[] data){
        Trace.beginSection("processData");
        float[] floatValues = new float[data.length];

        for (int i = 0; i < data.length-1; i++) {
            floatValues[i] = (int) data[i];
        }

        Trace.endSection();
        return floatValues;
    }

    private void feedInference(double[] floatValues){
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

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    public byte[] getMultiChannelArray(Mat m) {
        //first index is pixel, second index is channel
        int numChannels=m.channels();//is 3 for 8UC3 (e.g. RGB)
        int frameSize=m.rows()*m.cols();
        byte[] data= new byte[frameSize*numChannels];
        m.get(0,0,data);
        return data;
    }
}
