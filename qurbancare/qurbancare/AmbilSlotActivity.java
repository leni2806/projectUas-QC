package com.example.qurbancare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class AmbilSlotActivity extends AppCompatActivity {

    private ImageView imgSapi, btnBack;
    private ProgressBar progressBar;
    private ImageButton btnLanjut;

    private String namaHewanDiterima = "";
    private String slotDipilihUser = "";
    private long hargaPerSlot = 0;

    private TextView[] slotViews = new TextView[7];
    private int slotTerisiFirebase = 0;
    private int totalSlotMaksimal = 7; // 🌟 Tambahkan variabel total slot dinamis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambil_slot);

        imgSapi = findViewById(R.id.imgHewanAmbilSlot);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressAmbilSlot);
        btnLanjut = findViewById(R.id.btnLanjutPembayaran);

        // 1. TANGKAP DATA DINAMIS (Dari DetailHewanActivity)
        namaHewanDiterima = getIntent().getStringExtra("nama_hewan");
        String urlGambar = getIntent().getStringExtra("gambar_detail_sapi");
        slotTerisiFirebase = getIntent().getIntExtra("slot_terisi", 0);

        // 🌟 REVISI: Ambil total slot (1, 5, atau 7)
        totalSlotMaksimal = getIntent().getIntExtra("total_slot", 7);
        hargaPerSlot = getIntent().getLongExtra("harga_per_slot", 0);

        if (urlGambar != null) {
            Glide.with(this).load(urlGambar).into(imgSapi);
        }

        // 🌟 Set Progress Bar sesuai total slot asli (bukan dipatok 7 terus)
        if (progressBar != null) {
            progressBar.setMax(totalSlotMaksimal);
            progressBar.setProgress(slotTerisiFirebase);
        }

        setupSlotLogic();

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // 2. TOMBOL LANJUT
        if (btnLanjut != null) {
            btnLanjut.setOnClickListener(v -> {
                if (slotDipilihUser.isEmpty()) {
                    Toast.makeText(this, "Silakan pilih salah satu nomor slot!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(this, PembayaranActivity.class);
                    intent.putExtra("EXTRA_SLOT", slotDipilihUser);
                    intent.putExtra("EXTRA_PAKET", namaHewanDiterima);
                    intent.putExtra("EXTRA_HARGA", String.valueOf(hargaPerSlot));
                    startActivity(intent);
                }
            });
        }
    }

    private void setupSlotLogic() {
        for (int i = 0; i < 7; i++) {
            int resID = getResources().getIdentifier("slot" + (i + 1), "id", getPackageName());
            slotViews[i] = findViewById(resID);

            if (slotViews[i] == null) continue;

            // 🌟 LOGIKA KRUSIAL: Sembunyikan kotak jika di luar kuota (Revisi No 2 & 6)
            if (i >= totalSlotMaksimal) {
                slotViews[i].setVisibility(View.GONE);
                continue;
            } else {
                slotViews[i].setVisibility(View.VISIBLE);
            }

            // Logika Warna & Status Slot
            if (i < slotTerisiFirebase) {
                // Slot sudah terisi
                slotViews[i].setBackgroundResource(R.drawable.bg_slot_filled);
                slotViews[i].setOnClickListener(v ->
                        Toast.makeText(this, "Slot ini sudah diambil orang lain", Toast.LENGTH_SHORT).show());
            } else {
                // Slot masih kosong
                slotViews[i].setBackgroundResource(R.drawable.bg_slot_empty);
                int nomorSlot = i + 1;

                slotViews[i].setOnClickListener(v -> {
                    resetSlotWarna();
                    v.setBackgroundResource(R.drawable.bg_slot_filled);
                    slotDipilihUser = "Slot ke-" + nomorSlot;
                    Toast.makeText(this, "Terpilih: " + slotDipilihUser, Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void resetSlotWarna() {
        // Reset warna hanya untuk slot yang memang tersedia (sesuai kuota)
        for (int i = 0; i < totalSlotMaksimal; i++) {
            if (i >= slotTerisiFirebase) {
                if (slotViews[i] != null) {
                    slotViews[i].setBackgroundResource(R.drawable.bg_slot_empty);
                }
            }
        }
    }
}