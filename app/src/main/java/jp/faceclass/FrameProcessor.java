package jp.faceclass;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.List;

import io.fotoapparat.preview.Frame;
import jp.faceclass.nn.Detection;
import jp.faceclass.nn.DlibFaceDetecor;
import jp.faceclass.nn.TensorflowFaceClassifier;
import jp.faceclass.nn.TensorflowImageClassifier;

public class FrameProcessor implements io.fotoapparat.preview.FrameProcessor {

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private Handler classifyHandler;
    private HandlerThread classifyHandlerThread;

    private final DlibFaceDetecor dlibFaceDetecor;
    private TensorflowImageClassifier tfImageClassifier = null;
    private TensorflowFaceClassifier tfFaceClassifier = null;
    private final OnFacesDetectedListener listener;

    private String esd = Environment.getExternalStorageDirectory().getPath();
    private final String IMAGE_MODEL_FILE =esd + "/jp.faceclassifier/model/tensorflow_inception_graph.pb";
    private final String IMAGE_LABEL_FILE =esd + "/jp.faceclassifier/model/imagenet_comp_graph_label_strings.txt";
    private final int IMAGE_INPUT_SIZE = 224;
    private final int IMAGE_IMAGE_MEAN = 117;
    private final float IMAGE_IMAGE_STD = 1;
    private final String IMAGE_INPUT_NAME = "input";
    private final String IMAGE_OUTPUT_NAME = "output";

    private final String FACE_MODEL_FILE =esd + "/jp.faceclassifier/model/20170512-110547.pb";
    private final String FACE_LABEL_FILE = null;
    private final int FACE_INPUT_SIZE = 160;
    private final int FACE_IMAGE_MEAN = 117;
    private final float FACE_IMAGE_STD = 1;
    private final String FACE_INPUT_NAME = "input:0";
    private final String FACE_OUTPUT_NAME = "embeddings";

    private FrameProcessor(Builder builder) {


        classifyHandlerThread = new HandlerThread("tf");
        classifyHandlerThread.start();
        classifyHandler = new Handler(classifyHandlerThread.getLooper());

        dlibFaceDetecor = DlibFaceDetecor.create(esd + "/jp.faceclassifier/shape_predictor_5_face_landmarks.dat");
//        tfImageClassifier = TensorflowImageClassifier.create(IMAGE_MODEL_FILE, IMAGE_LABEL_FILE, IMAGE_INPUT_SIZE, IMAGE_IMAGE_MEAN, IMAGE_IMAGE_STD, IMAGE_INPUT_NAME, IMAGE_OUTPUT_NAME);

        tfFaceClassifier = TensorflowFaceClassifier.create(FACE_MODEL_FILE, FACE_LABEL_FILE, FACE_INPUT_SIZE, FACE_INPUT_NAME, FACE_OUTPUT_NAME);
        listener = builder.listener;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }
//
//    @Override
//    public void processFrame(Frame frame) {
//        if(DlibFaceDetecor.isProcessing){
//           return;
//        }
//
//        final List<Detection> faces = dlibFaceDetecor.detectFaces(
//                frame.image,
//                frame.size.width,
//                frame.size.height,
//                frame.rotation,
//                FACE_INPUT_SIZE
//        );
//
//        if(faces.size() > 0) {
//            mainThreadHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    listener.onFacesDetected(faces);
//                }
//            });
//        }
//        if(faces.size() > 0) {
//            classifyHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    tfFaceClassifier.classiyImage(faces.get(0).getImage());
//                }
//            });
//        }
//    }

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
                FACE_INPUT_SIZE
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
                    tfFaceClassifier.classiyImage(faces.get(0).getImage());
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
