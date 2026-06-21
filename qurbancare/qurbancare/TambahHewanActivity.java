package com.example.qurbancare;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TambahHewanActivity extends AppCompatActivity {

    private EditText etNama, etHarga, etBerat, etDeskripsi, etLinkGambar;
    private RadioGroup rgJenis;
    private Button btnSimpan, btnPilihGambar;
    private ImageView ivPreview;

    // Keperluan Upload Gambar
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_hewan);

        // 1. Inisialisasi Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // 2. Inisialisasi View
        etNama = findViewById(R.id.etNama);
        etHarga = findViewById(R.id.etHarga);
        etBerat = findViewById(R.id.etBerat);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        etLinkGambar = findViewById(R.id.etLinkGambar);
        rgJenis = findViewById(R.id.rgJenis);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnPilihGambar = findViewById(R.id.btnPilihGambar); // Pastikan ID ini ada di XML
        ivPreview = findViewById(R.id.ivPreview);           // Pastikan ID ini ada di XML

        // 3. Tombol Pilih Gambar
        btnPilihGambar.setOnClickListener(v -> pilihGambar());

        // 4. Tombol Simpan
        btnSimpan.setOnClickListener(v -> validasiDanSimpan());
    }

    private void pilihGambar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Foto Hewan"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            ivPreview.setImageURI(filePath); // Tampilkan preview foto yang dipilih
        }
    }

    private void validasiDanSimpan() {
        String nama = etNama.getText().toString().trim();
        String hargaStr = etHarga.getText().toString().trim();
        String berat = etBerat.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();
        String linkManual = etLinkGambar.getText().toString().trim();

        int selectedId = rgJenis.getCheckedRadioButtonId();
        if (selectedId == -1 || nama.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(this, "Nama, Harga, dan Jenis wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rb = findViewById(selectedId);
        String jenis = rb.getText().toString();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menyimpan data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // LOGIKA: Prioritaskan upload foto jika ada file yang dipilih
        if (filePath != null) {
            String fileName = "hewan/" + UUID.randomUUID().toString();
            StorageReference ref = storageReference.child(fileName);

            ref.putFile(filePath).addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    simpanFirestore(nama, hargaStr, berat, deskripsi, uri.toString(), jenis, progressDialog);
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Upload Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // Gunakan link manual jika tidak ada foto yang diupload
            simpanFirestore(nama, hargaStr, berat, deskripsi, linkManual, jenis, progressDialog);
        }
    }

    private void simpanFirestore(String nama, String harga, String berat, String desk, String url, String jenis, ProgressDialog pd) {
        Map<String, Object> data = new HashMap<>();
        data.put("nama", nama);
        data.put("harga", Long.parseLong(harga));
        data.put("berat", berat);
        data.put("deskripsi", desk);
        data.put("gambar", url); // URL dari Storage atau Input Manual
        data.put("jenis", jenis);

        db.collection("qurban").add(data)
                .addOnSuccessListener(documentReference -> {
                    pd.dismiss();
                    Toast.makeText(this, "Berhasil Menambah Hewan!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Gagal simpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}