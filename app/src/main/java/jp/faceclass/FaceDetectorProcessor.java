package jp.faceclass;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import jp.faceclass.detection.Detection;
import jp.faceclass.detection.TensorflowFaceDetector;


public class FaceDetectorProcessor implements FrameProcessor {

    private static Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());

//    private final TensorflowFaceClassifier tensorflowFaceClassifier;
    private final TensorflowFaceDetector tensorflowFaceDetector;
    private final OnFacesDetectedListener listener;

    private FaceDetectorProcessor(Builder builder) {
        tensorflowFaceDetector = new TensorflowFaceDetector();

        listener = builder.listener;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    @Override
    public void processFrame(Frame frame) {
        final List<Detection> faces = tensorflowFaceDetector.detectFaces(
                frame.image,
                frame.size.width,
                frame.size.height,
                frame.rotation
        );

        MAIN_THREAD_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                listener.onFacesDetected(faces);
            }
        });
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
     * Builder for {@link FaceDetectorProcessor}.
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

        public FaceDetectorProcessor build() {
            return new FaceDetectorProcessor(this);
        }

    }
}
