package raymond.infox;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.SparseArray;
import android.view.*;
import android.widget.Button;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;

public class ScanBarcodeActivity extends Activity {

    int bestWidth;
    int bestHeight;

    SurfaceView cameraPreview;
    Button button;
    CameraSource camSource;

    private static final int INTERNET_REQUEST_CODE = 99;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setupSystemUI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);

        cameraPreview = findViewById(R.id.camera_preview);


        button = findViewById(R.id.flashToggle);
        camSource = createCameraSource();

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Camera cam = getCamera(camSource);
                    Camera.Parameters p = cam.getParameters();
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        cam.setParameters(p);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        cam.setParameters(p);
                    }
                }
                return true;
            }
        });
    }

    // This function makes the status bar transparent.
    private void setupSystemUI() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    // Helper function, Finds and returns the Camera field in CameraSource class.
    private static Camera getCamera(CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    return (Camera) field.get(cameraSource);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }



    private void optimalResolution() {
//        Camera tmp = Camera.open(0);
//        List<Camera.Size> supportedResolutions = tmp.getParameters().getSupportedPreviewSizes();
//        double target = (double) cameraPreview.getWidth() / cameraPreview.getHeight();
//        Camera.Size bestSize = null;
//        for (Camera.Size size : supportedResolutions) {
//            if (bestSize == null) {
//                bestSize = size;
//            } else {
//                double currentBest = (double) bestSize.width / bestSize.height;
//                double thisRatio = (double) size.width / size.height;
//                if (Math.abs(target - thisRatio) < (Math.abs(target - currentBest))) {
//                    bestSize = size;
//                }
//            }
//        }
//        if (bestSize != null) {
//            bestWidth = bestSize.width;
//            bestHeight = bestSize.height;
//        } else {
//            bestWidth = 1000;
//            bestHeight = 2560;
//        }
//        tmp.stopPreview();
//        tmp.setPreviewCallback(null);
//        tmp.release();
    }


    // Builds camera source with appropriate width, height, handles surface changes. Uses google play API to
    // scan and store barcodes, and defines the behaviour when a barcode is detected
    private CameraSource createCameraSource() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
        final CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(2960  , 1440)
                .build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(ScanBarcodeActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ScanBarcodeActivity.this,
                            new String[]{android.Manifest.permission.CAMERA},
                            MY_CAMERA_REQUEST_CODE);
                    ActivityCompat.requestPermissions(ScanBarcodeActivity.this,
                            new String[]{Manifest.permission.INTERNET},
                            INTERNET_REQUEST_CODE);
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if(barcodes.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("barcode", barcodes.valueAt(0)); //gets latest barcode from array
                    setResult(CommonStatusCodes.SUCCESS, intent);
                }
            }
        });
        return cameraSource;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        createCameraSource();
    }
}
