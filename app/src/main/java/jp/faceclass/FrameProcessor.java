package jp.faceclass;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import io.fotoapparat.preview.Frame;
import jp.faceclass.nn.Detection;
import jp.faceclass.nn.DlibFaceDetecor;


public class FrameProcessor implements io.fotoapparat.preview.FrameProcessor {

    private static Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());

    private final DlibFaceDetecor dlibFaceDetecor;
//    private final TensorflowFaceClassifier tfFaceClassifier;
    private final OnFacesDetectedListener listener;

    private FrameProcessor(Builder builder) {
        dlibFaceDetecor = DlibFaceDetecor.create(Environment.getExternalStorageDirectory().getPath() + "/jp.faceclassifier/shape_predictor_5_face_landmarks.dat");
//        tfFaceClassifier = TensorflowFaceClassifier.create(builder.context.getAssets(), null, null, 0, null, null);
        listener = builder.listener;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    @Override
    public void processFrame(Frame frame) {
        if(DlibFaceDetecor.isProcessing){
           return;
        }

        final List<Detection> faces = dlibFaceDetecor.detectFaces(
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
