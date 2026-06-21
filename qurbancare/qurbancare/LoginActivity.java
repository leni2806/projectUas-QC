package com.example.qurbancare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etNomorWA, etPassword; // 🌟 Ganti etEmail jadi etNomorWA
    private ImageButton btnLogin;
    private TextView tvToRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🌟 Pastikan ID di XML activity_login sudah diganti jadi etNomorWA atau tetap etEmail tapi fungsinya buat WA
        etNomorWA = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvToRegister = findViewById(R.id.tvToRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String nomorWA = etNomorWA.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (nomorWA.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Nomor WA dan Password wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🌟 TRIK EMAIL BAYANGAN: Tambahkan domain otomatis agar nyambung dengan data Register
        String emailLogin;
        if (nomorWA.contains("@")) {
            // Jika yang login Admin pakai email asli (format excel)
            emailLogin = nomorWA;
        } else {
            // Jika bapak-bapak login pakai nomor WA
            emailLogin = nomorWA + "@qurbancare.com";
        }

        // 1. Login via Firebase Auth
        mAuth.signInWithEmailAndPassword(emailLogin, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(mAuth.getCurrentUser().getUid());
                    } else {
                        // 2. 🔥 JIKA GAGAL: Cek apakah ini Panitia dari Excel (Login manual pertama kali)
                        cekAktivasiExcel(emailLogin, pass);
                    }
                });
    }

    private void cekAktivasiExcel(String email, String pass) {
        db.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", pass)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        mAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(authResult -> {
                                    String newUid = authResult.getUser().getUid();
                                    DocumentSnapshot oldDoc = snapshots.getDocuments().get(0);

                                    db.collection("users").document(newUid).set(oldDoc.getData())
                                            .addOnSuccessListener(aVoid -> {
                                                db.collection("users").document(oldDoc.getId()).delete();
                                                checkUserRole(newUid);
                                                Toast.makeText(this, "Aktivasi Akun Panitia Berhasil!", Toast.LENGTH_LONG).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal Aktivasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Nomor WA atau Password salah!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String uid) {
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                pindahKeUtama(task.getResult());
            } else {
                db.collection("peserta").document(uid).get().addOnSuccessListener(docPeserta -> {
                    if (docPeserta.exists()) {
                        pindahKeUtama(docPeserta);
                    } else {
                        Toast.makeText(this, "Data User tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void pindahKeUtama(DocumentSnapshot doc) {
        String role = doc.getString("role");
        String nama = doc.getString("nama");

        Toast.makeText(this, "Selamat datang, " + nama, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, SapaActivity.class);
        intent.putExtra("USER_ROLE", role);
        startActivity(intent);
        finish();
    }
}