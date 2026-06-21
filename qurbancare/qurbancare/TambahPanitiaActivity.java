package com.example.qurbancare;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TambahPanitiaActivity extends AppCompatActivity {

    private EditText etNama, etEmail;
    private Spinner spRole;
    private Button btnSimpan;
    private ImageButton btnBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_panitia);

        // Inisialisasi
        db = FirebaseFirestore.getInstance();
        etNama = findViewById(R.id.etNamaBaru);
        etEmail = findViewById(R.id.etEmailBaru);
        spRole = findViewById(R.id.spRole);
        btnSimpan = findViewById(R.id.btnSimpanPanitia);
        btnBack = findViewById(R.id.btnBackTambah);

        // Setup Dropdown Role
        String[] roles = {"Admin", "Ketua Panitia", "Sekretaris", "Bendahara"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spRole.setAdapter(adapter);

        // Tombol Kembali
        btnBack.setOnClickListener(v -> finish());

        // Tombol Simpan
        btnSimpan.setOnClickListener(v -> {
            simpanKeFirestore();
        });
    }

    private void simpanKeFirestore() {
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String role = spRole.getSelectedItem().toString();

        // Validasi input kosong
        if (nama.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nama dan Email tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Data yang akan dikirim
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nama", nama);
        userMap.put("email", email);
        userMap.put("role", role);
        userMap.put("fotoUrl", ""); // Default kosong
        userMap.put("status", "Aktif");

        // Proses simpan ke koleksi "users"
        db.collection("users")
                .add(userMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(TambahPanitiaActivity.this, "Panitia Berhasil Ditambahkan!", Toast.LENGTH_SHORT).show();
                    finish(); // Tutup halaman dan balik ke daftar panitia
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TambahPanitiaActivity.this, "Gagal Simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}