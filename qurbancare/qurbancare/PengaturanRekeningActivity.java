package com.example.qurbancare;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class PengaturanRekeningActivity extends AppCompatActivity {

    private EditText etNamaBank, etNoRekening, etAtasNama;
    private Button btnSimpan;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan_rekening);

        etNamaBank = findViewById(R.id.etNamaBank);
        etNoRekening = findViewById(R.id.etNoRekening);
        etAtasNama = findViewById(R.id.etAtasNama);
        btnSimpan = findViewById(R.id.btnSimpanRekening);

        // Muat data dari Firebase biar kalau sudah ada isinya langsung tampil di kotak input
        muatDataRekeningLama();

        btnSimpan.setOnClickListener(v -> simpanKeFirebase());
        if (findViewById(R.id.btnBackRekening) != null) {
            findViewById(R.id.btnBackRekening).setOnClickListener(v -> finish());
        }
    }

    private void muatDataRekeningLama() {
        db.collection("pengaturan").document("rekening_bendahara")
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String bank = doc.getString("nama_bank");
                        String norek = doc.getString("nomor_rekening");
                        String an = doc.getString("atas_nama");

                        // Jika datanya bukan strip (-) atau kosong, tampilkan ke EditText
                        if (bank != null && !bank.equals("-")) etNamaBank.setText(bank);
                        if (norek != null && !norek.equals("-")) etNoRekening.setText(norek);
                        if (an != null && !an.equals("-")) etAtasNama.setText(an);
                    }
                });
    }

    private void simpanKeFirebase() {
        String bank = etNamaBank.getText().toString().trim();
        String norek = etNoRekening.getText().toString().trim();
        String an = etAtasNama.getText().toString().trim();

        if (bank.isEmpty() || norek.isEmpty() || an.isEmpty()) {
            Toast.makeText(this, "⚠️ Semua kolom input wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> dataRekening = new HashMap<>();
        dataRekening.put("nama_bank", bank);
        dataRekening.put("nomor_rekening", norek);
        dataRekening.put("atas_nama", an);

        db.collection("pengaturan").document("rekening_bendahara")
                .set(dataRekening)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Rekening Bendahara berhasil diperbarui!", Toast.LENGTH_LONG).show();
                    finish(); // Tutup halaman setelah sukses simpan
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "❌ Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}