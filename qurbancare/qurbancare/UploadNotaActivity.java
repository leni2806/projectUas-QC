package com.example.qurbancare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UploadNotaActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private EditText etNama, etHarga;
    private Button btnFoto, btnSimpan;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_nota);

        ivPreview = findViewById(R.id.ivPreviewNota);
        etNama = findViewById(R.id.etNamaBarang);
        etHarga = findViewById(R.id.etHargaNota);
        btnFoto = findViewById(R.id.btnAmbilFoto);
        btnSimpan = findViewById(R.id.btnSimpanNota);

        // Klik tombol ambil foto (Kamera/Galeri)
        btnFoto.setOnClickListener(v -> tampilkanPilihanSumber());

        // Klik tombol simpan
        btnSimpan.setOnClickListener(v -> {
            String nama = etNama.getText().toString();
            String harga = etHarga.getText().toString();

            if (nama.isEmpty() || harga.isEmpty() || ivPreview.getDrawable() == null) {
                Toast.makeText(this, "Lengkapi data dan foto nota!", Toast.LENGTH_SHORT).show();
            } else {
                prosesSimpanBase64(nama, harga);
            }
        });
    }

    private void tampilkanPilihanSumber() {
        String[] options = {"Kamera", "Galeri"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ambil Gambar Nota Melalui:");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                ivPreview.setImageBitmap(imageBitmap);
            } else if (requestCode == REQUEST_GALLERY) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    ivPreview.setImageBitmap(bitmap);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    private void prosesSimpanBase64(String nama, String harga) {
        Toast.makeText(this, "Sedang memproses data...", Toast.LENGTH_SHORT).show();

        try {
            // 1. Ambil Gambar dari ImageView
            ivPreview.setDrawingCacheEnabled(true);
            ivPreview.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) ivPreview.getDrawable()).getBitmap();

            // 2. Kompres Gambar & Ubah ke String Base64 (Agar bisa masuk Firestore)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Kompres ke 40% supaya ukuran string tidak melebihi limit Firestore (1MB)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
            byte[] byteImage = baos.toByteArray();
            String fotoKodeBase64 = Base64.encodeToString(byteImage, Base64.DEFAULT);

            // 3. Siapkan Data untuk Firestore
            String tanggal = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
            Map<String, Object> nota = new HashMap<>();
            nota.put("nama", nama);
            nota.put("total_harga", "Rp " + harga);
            nota.put("tanggal", tanggal);
            nota.put("status", "Lunas");
            nota.put("bukti", fotoKodeBase64); // String foto disimpan di field bukti
            nota.put("email", "");
            nota.put("tipe", "Pengeluaran");

            // 4. Kirim ke Koleksi "transaksi"
            db.collection("transaksi").add(nota)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Nota Berhasil Disimpan!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal Simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(this, "Terjadi kesalahan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}