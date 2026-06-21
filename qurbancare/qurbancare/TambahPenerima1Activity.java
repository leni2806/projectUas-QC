package com.example.qurbancare;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class TambahPenerima1Activity extends AppCompatActivity {

    private EditText etNama, etAlamat, etBerat;
    private Spinner spKategori;
    private Button btnSimpan;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_penerima1);

        etNama = findViewById(R.id.etNamaPenerima);
        etAlamat = findViewById(R.id.etAlamat);
        etBerat = findViewById(R.id.etBeratPaket);
        spKategori = findViewById(R.id.spKategori);
        btnSimpan = findViewById(R.id.btnSimpan);

        // Setup Spinner Kategori
        String[] kategori = {"Pilih Kategori", "Fakir", "Miskin", "Gharim", "Fi Sabilillah"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategori);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKategori.setAdapter(adapter);

        btnSimpan.setOnClickListener(v -> simpanKeFirebase());
    }

    private void simpanKeFirebase() {
        String nama = etNama.getText().toString().trim();
        String alamat = etAlamat.getText().toString().trim();
        String berat = etBerat.getText().toString().trim();
        String kategori = spKategori.getSelectedItem().toString();

        if (nama.isEmpty() || alamat.isEmpty() || berat.isEmpty() || kategori.equals("Pilih Kategori")) {
            Toast.makeText(this, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Buat Data
        Map<String, Object> data = new HashMap<>();
        data.put("nama", nama);
        data.put("alamat", alamat);
        data.put("berat", berat);
        data.put("kategori", kategori);
        data.put("status_ambil", "Proses"); // Status awal

        // 2. Simpan ke koleksi "penerima"
        db.collection("penerima")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Penerima berhasil ditambah!", Toast.LENGTH_SHORT).show();
                    finish(); // Tutup activity dan balik ke DistribusiActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}