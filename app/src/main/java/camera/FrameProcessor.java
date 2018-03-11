package camera;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.List;

import io.fotoapparat.preview.Frame;
import classification.Classifier;
import classification.Detection;
import classification.Detector;
import classification.Extractor;

public class FrameProcessor implements io.fotoapparat.preview.FrameProcessor {

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private Handler classifyHandler;

    private final Detector detector;
    private Extractor extractor;
    private Classifier classifier;
    private final OnFacesDetectedListener listener;

    private FrameProcessor(Builder builder) {

        HandlerThread classifyHandlerThread = new HandlerThread("tensorflow");
        classifyHandlerThread.start();
        classifyHandler = new Handler(classifyHandlerThread.getLooper());

        String appPath = Environment.getExternalStorageDirectory().getPath() + "/classifier/";

        detector = Detector.create(appPath + "dlib/shape_predictor_68_face_landmarks.dat");
        extractor = Extractor.create(appPath + "facenet/20170512-110547.pb");
        classifier = Classifier.create(appPath + "opencv/lfw_classifier_opencv.yml");

        listener = builder.listener;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }


    @Override
    public void process(Frame frame) {
        if(detector.isProcessing()){
            return;
        }

        final List<Detection> faces = detector.detectFaces(
                frame.getImage(),
                frame.getSize().width,
                frame.getSize().height,
                frame.getRotation(),
                Extractor.getInputSize()
        );

        mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFacesDetected(faces);
                }
            });

        if(faces.size() > 0) {
            if(extractor.isProcessing()) {
                return;
            }

            classifyHandler.post(new Runnable() {
                @Override
                public void run() {
                    float [] embeddings = extractor.extractEmbeddings(faces.get(0).getMat());
                    classifier.classify(embeddings);
                }
            });
        }
    }


    /**
     * Notified when faces are detected.
     */
    public interface OnFacesDetectedListener {

        /**
         * Null-object for {@link OnFacesDetectedListener}.
         */
        OnFacesDetectedListener NULL = new OnFacesDetectedListener() {
            @Override
            public void onFacesDetected(List<Detection> detections) {
                // Do nothing
            }
        };

        /**
         * Called when faces are detected. Always called on the main thread.
         *
         * @param detections detected faces. If no faces were detected - an empty list.
         */
        void onFacesDetected(List<Detection> detections);

    }

    /**
     * Builder for {@link FrameProcessor}.
     */
    public static class Builder {

        private final Context context;
        private OnFacesDetectedListener listener = OnFacesDetectedListener.NULL;

        private Builder(Context context) {
            this.context = context;
        }

        /**
         * @param listener which will be notified when faces are detected.
         */
        public Builder listener(OnFacesDetectedListener listener) {
            this.listener = listener != null ? listener : OnFacesDetectedListener.NULL;

            return this;
        }

        public FrameProcessor build() {
            return new FrameProcessor(this);
        }

    }

    float [] test_data(){
        double [] doubleArray = {-1.30232722e-01,  3.80396470e-02,  3.35903987e-02,  4.03830633e-02,
        -3.68488133e-02, -8.86956081e-02,  2.63451412e-02,  8.33301395e-02,
        6.38270304e-02,  5.45400828e-02, -1.71140775e-01, -1.37176393e-02,
        -9.95390117e-03, -5.85682951e-02,  1.80869624e-01, -1.58615578e-02,
        -5.49790375e-02,  1.66979065e-04,  3.91390920e-02,  7.67754838e-02,
        5.06210700e-02, -9.96657312e-02,  7.46516045e-03,  2.02350095e-01,
        -8.32180381e-02, -1.49542630e-01,  6.38552681e-02, -4.42493334e-02,
        -7.09155351e-02, -1.12867616e-01, -8.81564170e-02, -2.17649832e-01,
        -1.85516309e-02,  7.31686577e-02,  1.45159001e-02, -2.61539295e-02,
        -1.35845533e-02, -1.53943658e-01,  1.45666704e-01,  5.21797054e-02,
        -1.96531825e-02, -7.45467935e-03,  3.61730829e-02,  4.95644892e-03,
        -9.87873003e-02,  1.48555204e-01, -9.53203663e-02, -1.14072524e-01,
        -6.10261299e-02, -1.67058542e-01, -7.01780394e-02,  1.16218545e-01,
        2.93087238e-03,  5.72076030e-02, -4.11259830e-02, -2.88296640e-02,
        4.06560935e-02,  1.83780061e-03, -1.38160810e-01,  3.24934386e-02,
        6.73433021e-02,  6.15405254e-02, -8.02445412e-02, -5.76529726e-02,
        6.19145893e-02,  7.53113553e-02,  1.53957427e-01, -3.54837775e-02,
        -4.29813303e-02,  4.79971841e-02,  8.67431685e-02,  7.30950236e-02,
        1.30737558e-01, -1.18865661e-01,  5.40475249e-02,  3.44137847e-02,
        1.70265511e-02,  5.00205308e-02, -4.11257483e-02,  6.37386367e-02,
        -7.56190065e-03,  7.66831636e-02,  1.18642651e-01, -1.23396464e-01,
        -6.87649176e-02, -9.77851823e-03, -7.38723651e-02, -1.68742575e-02,
        -1.03513122e-01,  9.31880251e-02,  8.88885409e-02, -1.78107172e-02,
        -1.08035266e-01,  1.84774473e-01, -1.29341751e-01, -1.34011775e-01,
        -5.10582961e-02,  6.04406111e-02,  4.16649692e-02,  2.06202760e-01,
        8.79311375e-03,  2.66119912e-02,  1.07569592e-02,  2.48072922e-01,
        8.03420246e-02, -9.30341333e-02, -8.63488987e-02,  2.55976361e-03,
        -1.63753927e-01,  7.31339306e-02,  1.67444758e-02, -4.01508175e-02,
        6.48022518e-02, -7.08353668e-02, -1.27317294e-01, -4.66873422e-02,
        -2.32343208e-02,  1.10799879e-01,  1.16112694e-01, -2.08699349e-02,
        2.67519578e-02, -3.83303314e-02, -1.28780559e-01, -9.96180996e-02,
        -7.86279291e-02, -2.92007141e-02,  4.17220406e-02,  1.47390636e-02};
        float[] floatArray = new float[doubleArray.length];
        for (int i = 0 ; i < doubleArray.length; i++) {
            floatArray[i] = (float) doubleArray[i];
        }
        return floatArray;
    }
}
