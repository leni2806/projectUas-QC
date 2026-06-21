package com.example.qurbancare;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class InputPenerimaActivity extends AppCompatActivity {

    private EditText etNama, etAlamat, etPaket;
    private Button btnSimpan;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_penerima);

        etNama = findViewById(R.id.etNama);
        etAlamat = findViewById(R.id.etAlamat);
        etPaket = findViewById(R.id.etPaket);
        btnSimpan = findViewById(R.id.btnSimpanData);

        btnSimpan.setOnClickListener(v -> {
            String nama = etNama.getText().toString();
            String alamat = etAlamat.getText().toString();
            String paket = etPaket.getText().toString();

            if (nama.isEmpty() || alamat.isEmpty()) {
                Toast.makeText(this, "Nama dan Alamat wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Data yang akan dikirim ke Firestore
            Map<String, Object> user = new HashMap<>();
            user.put("nama", nama);
            user.put("alamat", alamat);
            user.put("paket", paket);
            user.put("status_ambil", "Proses"); // Default status
            user.put("petugas", "-"); // Belum ada petugas scan
            user.put("kategori", "Warga");

            db.collection("penerima").add(user)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Berhasil Menambah data!", Toast.LENGTH_SHORT).show();
                        finish(); // Balik ke halaman Distribusi
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}