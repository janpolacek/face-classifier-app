package classification;

import android.util.Log;

import org.opencv.core.Mat;
import org.tensorflow.Operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import tensorflow.TensorFlowInferenceInterface;

public class Extractor {
    private static final String TAG = "Extractor";
    private static final int inputSize = 160;
    private static final String inputName = "input";
    private static final String outputName = "embeddings";
    private boolean processing = false;

    // Pre-allocated buffers.
    private float[] output;
    private String[] outputNames;
    private int outputSize;

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
//        Mat m = imread("/sdcard/classifier/detections/chip_0.png");
        inferenceInterface.feed(inputName, processMat(mat), 1, inputSize, inputSize, 3);
        inferenceInterface.feed("phase_train", false);
        inferenceInterface.run(outputNames, false);
        inferenceInterface.fetch(outputName, output);

        long estimatedTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "extraction time:" + estimatedTime);
        setProcessing(false);
        return output;
    }

    private float [] convertBytesToFloat(byte[] data){
        float[] floatValues = new float[data.length];
        for (int i = 0; i < data.length-1; i++) {
            floatValues[i] = (data[i] & 0xff);
        }
        return floatValues;
    }


    private float [] substractAndMultiply(float [] floats, float substract, float multiply){
        for(int i = 0; i<floats.length; i++){
            floats[i] = (floats[i] - substract)*multiply;
        }

        return floats;
    }

    private float [] prewhiten(float [] data) {
        Statistics statistics = new Statistics(data);
        float mean =statistics.getMean();
        float std = statistics.getStdDev();
        float std_adj = (float) Math.max(std,  (1/Math.sqrt(data.length)));
        data = substractAndMultiply(data, mean, (1/std_adj));
        return data;
    }


    private float[] processMat(Mat m) {
        int numChannels=m.channels();//is 3 for 8UC3 (e.g. RGB)
        int frameSize=m.rows()*m.cols();
        byte[] data= new byte[frameSize*numChannels];
        m.get(0,0,data);
        return prewhiten(convertBytesToFloat(data));
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    public void close() {
        inferenceInterface.close();
    }
    public static int getInputSize() {
        return inputSize;
    }

}
