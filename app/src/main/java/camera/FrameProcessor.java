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

        detector = Detector.create(appPath + "dlib/shape_predictor_5_face_landmarks.dat");
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
//                    classifier.classify(embeddings);
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
}
