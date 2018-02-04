package jp.faceclass;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.List;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.selector.ResolutionSelectorsKt;
import io.fotoapparat.view.CameraView;
import jp.faceclass.nn.Detection;

import static io.fotoapparat.log.LoggersKt.fileLogger;
import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.continuousFocusPicture;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.ResolutionSelectorsKt.highestResolution;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;

public class CameraActivity extends AppCompatActivity {

    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasCameraPermission;
    private CameraView cameraView;
    private DetectionsView detectionsView;

    private Fotoapparat backFotoapparat;

    private String TAG = "CameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getSupportActionBar().hide();

        cameraView = (CameraView) findViewById(R.id.camera_view);
        detectionsView = (DetectionsView) findViewById(R.id.rectanglesView);

        hasCameraPermission = permissionsDelegate.hasCameraPermission();

        if (hasCameraPermission) {
            cameraView.setVisibility(View.VISIBLE);
        } else {
            permissionsDelegate.requestCameraPermission();
        }

        setupFotoapparat();
        focusOnLongClick();
    }

    public void setupFotoapparat() {
        backFotoapparat = createFotoapparat();
    }

    public void focusOnLongClick() {
        cameraView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }


    private Fotoapparat createFotoapparat() {
        return Fotoapparat
                .with(this)
                .into(cameraView)
                .previewScaleType(ScaleType.CenterInside)
                .photoResolution(highestResolution())
                .lensPosition(back())
                .focusMode(firstAvailable(
                        continuousFocusPicture(),
                        autoFocus(),
                        fixed()
                ))
//                .previewFpsRange(rangeWithHighestFps())
//                .sensorSensitivity(highestSensorSensitivity())
                .frameProcessor(FrameProcessor.with(this)
                        .listener(new FrameProcessor.OnFacesDetectedListener() {
                            @Override
                            public void onFacesDetected(List<Detection> detections) {
                                Log.d("TAG", "Detected faces: " + detections.size());
                                detectionsView.setRectangles(detections);
                            }
                        })
                        .build())
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .build();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (hasCameraPermission) {
            backFotoapparat.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (hasCameraPermission) {
            backFotoapparat.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {
            backFotoapparat.start();
            cameraView.setVisibility(View.VISIBLE);
        }
    }

}
