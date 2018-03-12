package classification;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.SVM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Classifier {
    private static final String TAG = "Classifier";

    private final int inputSize = 128;
    private Mat samples = new Mat(1, inputSize, CvType.CV_32F);
    private Mat results = new Mat();
    private SVM svm;
    private ArrayList<String> labels = new ArrayList<>();
    private Classifier() {}

    public static Classifier create(String modelFilename, String labelsFilename) {
        Classifier c = new Classifier();
        c.svm = SVM.load(modelFilename);
        c.labels = loadLabels(labelsFilename);
        return c;
    }

    public String classify(final float[] embeddings) {
        samples.put(0, 0, embeddings);
        svm.predict(samples, results, 0);
        int predictedClass = (int) results.get(0, 0)[0];
        Log.d(TAG, "predicted class: " +  predictedClass + ", " + labels.get(predictedClass));
        return labels.get(predictedClass);
    }

    private static ArrayList<String> loadLabels(String fileName){
        final File file = new File(fileName);
        ArrayList<String> labels = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return labels;
    }

    public void close() {
    }
}
