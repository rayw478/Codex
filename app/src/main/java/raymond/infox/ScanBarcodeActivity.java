package raymond.infox;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

public class ScanBarcodeActivity extends Activity {

    SurfaceView cameraPreview;
    Button button;
    CameraSource camSource;
    String code;
    int orientation;

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int WRITE_REQUEST_CODE = 101;
    private static final int READ_REQUEST_CODE = 102;
    private static final int PICTURE_RETURN_CODE = 999;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setupSystemUI();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        cameraPreview = findViewById(R.id.camera_preview);
        button = findViewById(R.id.flashToggle);
        camSource = createCameraSource();
        if (!getIntent().getStringExtra("barcode").equals("")) {
            code = getIntent().getStringExtra("barcode");
        } else {
            code = null;
        }

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Camera cam = getCamera(camSource);
                    assert cam != null;
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (code == null) {
                    Toast.makeText(ScanBarcodeActivity.this, "Scan a barcode first.", Toast.LENGTH_SHORT).show();
                } else {
                    takePhoto(view);
                }
            }
        });
    }

    private void takePhoto(View view) {
        camSource.takePicture(new CameraSource.ShutterCallback() {
            @Override
            public void onShutter() {
                orientation = getResources().getConfiguration().orientation;
            }
        }, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                Intent intent = new Intent();
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    bmp = rotatePortrait(bmp);
                }
                String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Codex/Photos";
                File dir = new File(file_path);
                if (!dir.exists())
                    dir.mkdirs();
                File file = new File(dir, code + ".jpg");
                try {
                    FileOutputStream fOut = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                    fOut.flush();
                    fOut.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra("imagePath", file.getAbsolutePath());
                intent.putExtra("barcode", code);
                setResult(PICTURE_RETURN_CODE, intent);
                finish();
            }
        });
    }

    private Bitmap rotatePortrait(Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bmp, 0,0, bmp.getWidth(), bmp.getHeight(), matrix, true);
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
        return null; // should not return null
    }

    // Builds camera source with appropriate width, height, handles surface changes. Uses google play API to
    // scan and store barcodes, and defines the behaviour when a barcode is detected
    private CameraSource createCameraSource() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
        final CameraSource cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(height  ,width)
                .setRequestedFps(60)
                .build();

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(ScanBarcodeActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ScanBarcodeActivity.this,
                            new String[]{android.Manifest.permission.CAMERA},
                            MY_CAMERA_REQUEST_CODE);
                    ActivityCompat.requestPermissions(ScanBarcodeActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_REQUEST_CODE);
                    ActivityCompat.requestPermissions(ScanBarcodeActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_REQUEST_CODE);
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
                    code = barcodes.valueAt(0).displayValue; //gets latest barcode from array
                    Intent intent = new Intent();
                    intent.putExtra("barcode", code);
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
