package camera;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final OnFrameProcessedListener listener;

    private FrameProcessor(Builder builder) {

        HandlerThread classifyHandlerThread = new HandlerThread("tensorflow");
        classifyHandlerThread.start();
        classifyHandler = new Handler(classifyHandlerThread.getLooper());

        String appPath = Environment.getExternalStorageDirectory().getPath() + "/classifier/";

        detector = Detector.create(appPath + "dlib/shape_predictor_5_face_landmarks.dat");
        extractor = Extractor.create(appPath + "facenet/20170512-110547-optimized.pb");
        classifier = Classifier.create(appPath + "opencv/lfw_classifier_opencv.yml", appPath + "opencv/classifier_pairs.txt");

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
                    if(faces.size() == 0){
                        //vycistenie textu ak nemame tvare
                        listener.onFacesClassified("");
                    }
                }
            });

        if(faces.size() == 0) return;
        if(extractor.isProcessing()) return;

        classifyHandler.post(new Runnable() {
            @Override
            public void run() {
//                Kod pre clasifikaciu jednej tvare
//                float [] embeddings = extractor.extractEmbeddings(faces.get(0).getMat());
//                final String [] classified = classifier.classify(embeddings);
//                mainThreadHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        listener.onFacesClassified(classified);
//                    }
//                });

//                Kod pre clasifikaciu viacerych tvari
                float [] embeddings_list = extractor.extractMultipleEmbeddings(faces);
                final String [] classified_list = classifier.classifyMultiple(embeddings_list);
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFacesClassified(Arrays.toString(classified_list));
                    }
                });
            }
        });
    }


    /**
     * Notified when faces are detected.
     */
    public interface OnFrameProcessedListener {
        OnFrameProcessedListener NULL = new OnFrameProcessedListener() {
            @Override
            public void onFacesDetected(List<Detection> detections) {// Do nothing
            }

            @Override
            public void onFacesClassified(String classifications) {// Do nothing
            }

        };
        void onFacesDetected(List<Detection> detections);
        void onFacesClassified(String classifications);

    }

    /**
     * Builder for {@link FrameProcessor}.
     */
    public static class Builder {

        private final Context context;
        private OnFrameProcessedListener listener = OnFrameProcessedListener.NULL;

        private Builder(Context context) {
            this.context = context;
        }

        /**
         * @param listener which will be notified when faces are detected.
         */
        public Builder listener(OnFrameProcessedListener listener) {
            this.listener = listener != null ? listener : OnFrameProcessedListener.NULL;
            return this;
        }

        public FrameProcessor build() {
            return new FrameProcessor(this);
        }

    }
}
