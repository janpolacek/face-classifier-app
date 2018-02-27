package jp.faceclass;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.List;

import io.fotoapparat.preview.Frame;
import jp.faceclass.nn.Classifier;
import jp.faceclass.nn.Detection;
import jp.faceclass.nn.DlibFaceDetecor;
import jp.faceclass.nn.EmbeddingsExtractor;

public class FrameProcessor implements io.fotoapparat.preview.FrameProcessor {

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private Handler classifyHandler;

    private final DlibFaceDetecor dlibFaceDetecor;
    private EmbeddingsExtractor embeddingsExtractor;
    private Classifier classifier;
    private final OnFacesDetectedListener listener;

    private FrameProcessor(Builder builder) {

        HandlerThread classifyHandlerThread = new HandlerThread("tf");
        classifyHandlerThread.start();
        classifyHandler = new Handler(classifyHandlerThread.getLooper());

        String envDirectory = Environment.getExternalStorageDirectory().getPath();
        dlibFaceDetecor = DlibFaceDetecor.create(envDirectory + "/jp.faceclassifier/shape_predictor_5_face_landmarks.dat");
        embeddingsExtractor = EmbeddingsExtractor.create(envDirectory + "/jp.faceclassifier/model/20170512-110547.pb");
//        classifier = Classifier.create(envDirectory + "/jp.faceclassifier/1519740686");

        listener = builder.listener;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }


    @Override
    public void process(Frame frame) {
        if(DlibFaceDetecor.isProcessing){
            return;
        }

        final List<Detection> faces = dlibFaceDetecor.detectFaces(
                frame.getImage(),
                frame.getSize().width,
                frame.getSize().height,
                frame.getRotation(),
                EmbeddingsExtractor.getInputSize()
        );

        if(faces.size() > 0) {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFacesDetected(faces);
                }
            });
        }
        if(faces.size() > 0) {
            classifyHandler.post(new Runnable() {
                @Override
                public void run() {
                    embeddingsExtractor.runModel(faces.get(0).getImage());
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
