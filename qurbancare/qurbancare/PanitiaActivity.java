package com.example.qurbancare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanitiaActivity extends AppCompatActivity {

    private RecyclerView rvPanitia;
    private PanitiaAdapter adapter;
    private List<Panitia> panitiaList;
    private EditText etSearch;
    private FloatingActionButton fabAdd, fabImportPanitia; // 🌟 Tambah variabel import
    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // 🌟 Untuk daftar akun

    // 🌟 Launcher untuk milih file CSV
    private final ActivityResultLauncher<Intent> csvPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        prosesImportCSVPanitia(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panitia);

        // 1. Inisialisasi
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        rvPanitia = findViewById(R.id.rvPanitia);
        etSearch = findViewById(R.id.etSearchPanitia);
        fabAdd = findViewById(R.id.fabTambah);
        fabImportPanitia = findViewById(R.id.fabImportExcel); // 🌟 Sesuaikan ID di XML

        // 2. Setup RecyclerView
        panitiaList = new ArrayList<>();
        adapter = new PanitiaAdapter(panitiaList, this);
        rvPanitia.setLayoutManager(new LinearLayoutManager(this));
        rvPanitia.setAdapter(adapter);

        // 3. Tombol Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 4. Tombol Tambah Manual
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PanitiaActivity.this, TambahPanitiaActivity.class);
            startActivity(intent);
        });

        // 5. Tombol Import CSV
        if (fabImportPanitia != null) {
            fabImportPanitia.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/*");
                csvPickerLauncher.launch(Intent.createChooser(intent, "Pilih File CSV Panitia"));
            });
        }

        cekRoleAdmin();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataPanitia();
    }

    // 🌟 MESIN IMPORT PANITIA (Anti-Crash & Langsung Daftar Akun)
    private void prosesImportCSVPanitia(Uri fileUri) {
        Toast.makeText(this, "Sedang mendaftarkan akun panitia...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                boolean isHeader = true;
                int count = 0;

                while ((line = reader.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }

                    // Gunakan pemisah ; sesuai file user.csv kamu
                    String[] t = line.split(";", -1);
                    if (t.length >= 5) {
                        String nama = t[0].replace("\"", "").trim();
                        String email = t[1].replace("\"", "").trim();
                        String role = t[2].replace("\"", "").trim();
                        String bagian = t[3].replace("\"", "").trim();
                        String pass = t[4].replace("\"", "").trim();
                        String foto = t.length > 5 ? t[5].replace("\"", "") : "";

                        if (email.isEmpty() || pass.length() < 6) continue;

                        // Eksekusi Daftar Akun
                        mAuth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(res -> {
                            String uid = res.getUser().getUid();
                            Map<String, Object> u = new HashMap<>();
                            u.put("uid", uid);
                            u.put("nama", nama);
                            u.put("email", email);
                            u.put("role", role);
                            u.put("bagian", bagian);
                            u.put("password", pass);
                            u.put("fotoUrl", foto);

                            db.collection("users").document(uid).set(u);
                        }).addOnFailureListener(e -> Log.e("AUTH", "Gagal daftar: " + email));

                        count++;
                    }
                }
                reader.close();
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Selesai! Tunggu sebentar agar data muncul.", Toast.LENGTH_LONG).show();
                    loadDataPanitia();
                });
            } catch (Exception e) {
                Log.e("IMPORT_CSV", e.getMessage());
            }
        }).start();
    }

    private void cekRoleAdmin() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String role = doc.getString("role");
                    boolean isAdmin = "Admin".equalsIgnoreCase(role);
                    fabAdd.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    if (fabImportPanitia != null) {
                        fabImportPanitia.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }
    }

    private void loadDataPanitia() {
        db.collection("users").get().addOnSuccessListener(snapshots -> {
            panitiaList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                try {
                    Panitia p = doc.toObject(Panitia.class);
                    if (p.getRole() != null && (p.getRole().equalsIgnoreCase("Admin") || p.getRole().equalsIgnoreCase("Panitia"))) {
                        panitiaList.add(p);
                    }
                } catch (Exception e) {
                    Log.e("LOAD", "Error satu data");
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().toLowerCase();
                List<Panitia> filtered = new ArrayList<>();
                for (Panitia p : panitiaList) {
                    if ((p.getNama() != null && p.getNama().toLowerCase().contains(keyword)) ||
                            (p.getBagian() != null && p.getBagian().toLowerCase().contains(keyword))) {
                        filtered.add(p);
                    }
                }
                adapter.updateList(filtered);
            }
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}