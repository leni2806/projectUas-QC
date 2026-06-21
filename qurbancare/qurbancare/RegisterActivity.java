package com.example.qurbancare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNama, etPassword, etNomorWA; // 🌟 Email dihapus dari UI
    private ImageButton btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNama = findViewById(R.id.etRegNama);
        etPassword = findViewById(R.id.etRegPassword);
        etNomorWA = findViewById(R.id.etRegNomorWA); // 🌟 Bapak-bapak input ini sebagai ID
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> validasiDanDaftar());
    }

    private void validasiDanDaftar() {
        String nama = etNama.getText().toString().trim();
        String nomorWA = etNomorWA.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // Validasi: Semua field harus diisi
        if (nama.isEmpty() || nomorWA.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Harap isi Nama, Nomor WA, dan Password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 6) {
            Toast.makeText(this, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🌟 TRIK EMAIL BAYANGAN: Ubah nomor WA jadi format email untuk Firebase
        String emailBayangan = nomorWA + "@qurbancare.com";

        registerPeserta(nama, emailBayangan, pass, nomorWA);
    }

    private void registerPeserta(String nama, String emailBayangan, String pass, String nomorWA) {
        mAuth.createUserWithEmailAndPassword(emailBayangan, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Simpan ke Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("uid", uid);
                        userData.put("nama", nama);
                        userData.put("email", emailBayangan); // Simpan email bayangan
                        userData.put("nomorWA", nomorWA);    // Simpan nomor asli bapak-bapak
                        userData.put("role", "Peserta");

                        Map<String, Object> pesertaData = new HashMap<>();
                        pesertaData.put("id", uid);
                        pesertaData.put("nama", nama);
                        pesertaData.put("nomorWA", nomorWA);
                        pesertaData.put("status", "Proses");

                        db.collection("users").document(uid).set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    db.collection("peserta").document(uid).set(pesertaData)
                                            .addOnSuccessListener(aVoid2 -> {
                                                Toast.makeText(this, "Berhasil Daftar! Silakan Login dengan No. WA", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            });
                                });
                    } else {
                        Toast.makeText(this, "Gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}