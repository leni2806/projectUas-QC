package com.example.qurbancare;

import android.os.Bundle;
import android.view.View;
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

public class TambahPesertaActivity extends AppCompatActivity {

    private EditText etNama, etEmail, etSlot;
    private Spinner spPaket;
    private Button btnSimpan;
    private ImageButton btnBack;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_peserta);

        // 1. Inisialisasi View sesuai ID di XML kamu
        etNama = findViewById(R.id.etNamaPesertaBaru);
        etEmail = findViewById(R.id.etEmailPesertaBaru);
        etSlot = findViewById(R.id.etSlotBaru);
        spPaket = findViewById(R.id.spPaket);
        btnSimpan = findViewById(R.id.btnSimpanPesertaManual);
        btnBack = findViewById(R.id.btnBackTambahPeserta);

        // 2. Setup Spinner (Pilihan Paket)
        String[] opsiPaket = {"Sapi ke 1", "Sapi ke 2", "Kambing ke 1", "Kambing ke 2"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, opsiPaket);
        spPaket.setAdapter(adapterSpinner);

        // 3. Tombol Back
        btnBack.setOnClickListener(v -> finish());

        // 4. Tombol Simpan
        btnSimpan.setOnClickListener(v -> simpanKeFirebase());
    }

    private void simpanKeFirebase() {
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String slot = etSlot.getText().toString().trim();
        String paket = spPaket.getSelectedItem().toString();

        // Validasi: Jangan sampai ada yang kosong
        if (nama.isEmpty() || email.isEmpty() || slot.isEmpty()) {
            Toast.makeText(this, "Wajib isi semua data ya!", Toast.LENGTH_SHORT).show();
            return;
        }

        // BUNGKUS DATA: Sesuai dengan field yang ada di koleksi "transaksi"
        Map<String, Object> dataPeserta = new HashMap<>();
        dataPeserta.put("nama", nama);
        dataPeserta.put("email", email);
        dataPeserta.put("paket", paket);
        dataPeserta.put("slot", slot);
        dataPeserta.put("status", "Lunas"); // Karena Admin yang input, kita set Lunas saja

        // SIMPAN KE FIRESTORE
        db.collection("transaksi")
                .add(dataPeserta)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Berhasil menambah peserta!", Toast.LENGTH_SHORT).show();
                    finish(); // OTOMATIS BALIK ke halaman sebelumnya (PesertaActivity)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}