package classification;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.ml.SVM;

public class Classifier {
    private static final String TAG = "Classifier";

    private final int inputSize = 128;
    private Mat samples = new Mat(1, inputSize, CvType.CV_32F);
    private Mat results = new Mat();
    private SVM svm;
    private String [] labels = {"Ariel Sharon", "Arnold Schwarzenegger", "Colin Powell",
            "Donald Rumsfeld", "George W Bush", "Gerhard Schroeder",
            "Gloria Macapagal Arroyo", "Hugo Chavez", "Jacques Chirac",
            "Jean Chretien", "Jennifer Capriati", "John Ashcroft",
            "Junichiro Koizumi", "Laura Bush", "Lleyton Hewitt",
            "Luiz Inacio Lula da Silva", "Serena Williams", "Tony Blair",
            "Vladimir Putin"};
    private Classifier() {}

    public static Classifier create(String modelFilename) {
        Classifier c = new Classifier();
        c.svm = SVM.load(modelFilename);
        return c;
    }

    public int classify(final float[] embeddings) {
        samples.put(0, 0, embeddings);
        svm.predict(samples, results, 0);
        int predictedClass = (int) results.get(0, 0)[0];
        Log.d(TAG, "predicted class: " +  predictedClass + ", " + labels[predictedClass]);
        return predictedClass;
    }

    public void close() {
    }
}
