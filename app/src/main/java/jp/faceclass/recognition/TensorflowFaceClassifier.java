//package jp.faceclass.recognition;
//
//import android.content.res.AssetManager;
//import android.util.Log;
//
//import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.PriorityQueue;
//import java.util.Vector;
//
//public class TensorflowFaceClassifier {
//    private static final String TAG = "TFImageClassifier";
//
//    // Only return this many results with at least this confidence.
//    private static final int MAX_RESULTS = 3;
//    private static final float THRESHOLD = 0.1f;
//
//    // Config values.
//    private String inputName;
//    private String outputName;
//    private int inputSize;
//
//    // Pre-allocated buffers.
//    private Vector<String> labels = new Vector<>();
//    private float[] outputs;
//    private String[] outputNames;
//
//    private TensorFlowInferenceInterface inferenceInterface;
//
//    private boolean runStats = false;
//
//    private TensorflowFaceClassifier() {
//    }
//
//    /**
//     * Initializes a native TensorFlow session for classifying images.
//     *
//     * @param assetManager  The asset manager to be used to load assets.
//     * @param modelFilename The filepath of the model GraphDef protocol buffer.
//     * @param labelFilename The filepath of label file for classes.
//     * @param inputSize     The input size. A square image of inputSize x inputSize is assumed.
//     * @param inputName     The label of the image input node.
//     * @param outputName    The label of the output node.
//     * @throws IOException
//     */
//    public static void create(
//            AssetManager assetManager,
//            String modelFilename,
//            String labelFilename,
//            int inputSize,
//            String inputName,
//            String outputName)
//            throws IOException {
//        TensorflowFaceClassifier c = new TensorflowFaceClassifier();
//        c.inputName = inputName;
//        c.outputName = outputName;
//
//        // Read the label names into memory.
//        // TODO(andrewharp): make this handle non-assets.
//        String actualFilename = labelFilename.split("file:///android_asset/")[1];
//        Log.i(TAG, "Reading labels from: " + actualFilename);
//        BufferedReader br = null;
//        br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
//        String line;
//        while ((line = br.readLine()) != null) {
//            c.labels.add(line);
//        }
//        br.close();
//
//        c.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
//
//        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
//        int numClasses =
//                (int) c.inferenceInterface.graph().operation(outputName).output(0).shape().size(1);
//        Log.i(TAG, "Read " + c.labels.size() + " labels, output layer size is " + numClasses);
//
//        // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
//        // the placeholder node for input in the graphdef typically used does not specify a shape, so it
//        // must be passed in as a parameter.
//        c.inputSize = inputSize;
//
//        // Pre-allocate buffers.
//        c.outputNames = new String[]{outputName};
//        c.outputs = new float[numClasses];
//
//        return c;
//    }
//
//    @Override
//    public List<Recognition> recognizeImage(final float[] pixels) {
//        inferenceInterface.feed(inputName, pixels, new long[]{inputSize * inputSize});
//        inferenceInterface.run(outputNames, runStats);
//        inferenceInterface.fetch(outputName, outputs);
//
//        // Find the best classifications.
//        PriorityQueue<Recognition> pq =
//                new PriorityQueue<>(
//                        3,
//                        new Comparator<Recognition>() {
//                            @Override
//                            public int compare(Recognition lhs, Recognition rhs) {
//                                // Intentionally reversed to put high confidence at the head of the queue.
//                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
//                            }
//                        });
//        for (int i = 0; i < outputs.length; ++i) {
//            if (outputs[i] > THRESHOLD) {
//                pq.add(
//                        new Recognition(
//                                "" + i, labels.size() > i ? labels.get(i) : "unknown", outputs[i], null));
//            }
//        }
//
//        final ArrayList<Recognition> recognitions = new ArrayList<>();
//        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
//        for (int i = 0; i < recognitionsSize; ++i) {
//            recognitions.add(pq.poll());
//        }
//
//        return recognitions;
//    }
//
//    @Override
//    public void enableStatLogging(boolean debug) {
//        runStats = debug;
//    }
//
//    @Override
//    public String getStatString() {
//        return inferenceInterface.getStatString();
//    }
//
//    @Override
//    public void close() {
//        inferenceInterface.close();
//    }
//
//}
