package com.example.qurbancare;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import java.util.concurrent.ExecutionException;

public class CrowdDetectionActivity extends AppCompatActivity {
    private PreviewView previewView;
    private TextView tvCount, tvStatus;
    private ObjectDetector detector;
    private View boundingBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crowd_detection);

        previewView = findViewById(R.id.previewView);
        tvCount = findViewById(R.id.tvCount);
        tvStatus = findViewById(R.id.tvStatus);
        boundingBox = findViewById(R.id.boundingBox);

        findViewById(R.id.btnBackCrowd).setOnClickListener(v -> finish());

        // 🔥 LOGIKA BARU: Mode STREAM dan Multiple Objects agar lebih sensitif
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build();
        detector = ObjectDetection.getClient(options);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(this);
        providerFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = providerFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Set resolusi yang seimbang agar AI tidak lemot tapi akurat
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
                    @Override
                    @androidx.camera.core.ExperimentalGetImage
                    public void analyze(@NonNull androidx.camera.core.ImageProxy imageProxy) {
                        if (imageProxy.getImage() == null) return;

                        InputImage inputImage = InputImage.fromMediaImage(
                                imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

                        detector.process(inputImage).addOnSuccessListener(objects -> {
                            // Hitung semua objek yang terdeteksi sebagai "orang" atau benda umum
                            int totalFound = objects.size();

                            if (totalFound > 0) {
                                // Ambil objek pertama untuk digambar kotaknya
                                android.graphics.Rect rect = objects.get(0).getBoundingBox();
                                updateBoundingBox(rect, imageProxy.getWidth(), imageProxy.getHeight());
                            } else {
                                hideBoundingBox();
                            }
                            updateUI(totalFound);
                        }).addOnCompleteListener(t -> imageProxy.close());
                    }
                });

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Gagal membuka kamera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void updateBoundingBox(android.graphics.Rect rect, int imgWidth, int imgHeight) {
        runOnUiThread(() -> {
            boundingBox.setVisibility(View.VISIBLE);

            // 🔥 TRIK MATEMATIKA: Menyesuaikan koordinat gambar AI ke layar HP
            float viewWidth = previewView.getWidth();
            float viewHeight = previewView.getHeight();

            float scaleX = viewWidth / (float) imgHeight; // Karena rotasi kamera
            float scaleY = viewHeight / (float) imgWidth;

            boundingBox.setX(rect.left * scaleX);
            boundingBox.setY(rect.top * scaleY);

            android.view.ViewGroup.LayoutParams params = boundingBox.getLayoutParams();
            params.width = (int) (rect.width() * scaleX);
            params.height = (int) (rect.height() * scaleY);
            boundingBox.setLayoutParams(params);
        });
    }

    private void hideBoundingBox() {
        runOnUiThread(() -> boundingBox.setVisibility(View.GONE));
    }

    private void updateUI(int count) {
        runOnUiThread(() -> {
            tvCount.setText("Terdeteksi: " + count + " Orang");
            if (count > 8) {
                tvStatus.setText("STATUS: TERLALU RAMAI");
                tvStatus.setTextColor(android.graphics.Color.RED);
            } else if (count > 0) {
                tvStatus.setText("STATUS: AREA AMAN");
                tvStatus.setTextColor(android.graphics.Color.GREEN);
            } else {
                tvStatus.setText("STATUS: MEMANTAU...");
                tvStatus.setTextColor(android.graphics.Color.WHITE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }
}