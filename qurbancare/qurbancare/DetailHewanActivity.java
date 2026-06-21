package com.example.qurbancare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.Locale;

public class DetailHewanActivity extends AppCompatActivity {

    private ImageView imgHewan, btnBack;
    private TextView tvNama, tvBerat, tvHarga, tvDeskripsi, tvStatusSlot, tvUmur, tvJenisKelamin, tvInfoHarga;
    private ProgressBar progressPatungan;
    private ImageButton btnPatungan;
    private View layoutSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_hewan);

        // 1. Inisialisasi View
        imgHewan = findViewById(R.id.imgHewanDetail);
        btnBack = findViewById(R.id.btnBack);
        tvNama = findViewById(R.id.tvNamaDetail);
        tvBerat = findViewById(R.id.tvBeratDetail);
        tvHarga = findViewById(R.id.tvHargaDetail);
        tvDeskripsi = findViewById(R.id.tvDeskripsiDetail);
        tvStatusSlot = findViewById(R.id.tvStatusSlot);
        progressPatungan = findViewById(R.id.progressPatungan);
        btnPatungan = findViewById(R.id.btnLihatDetailFigma);
        tvInfoHarga = findViewById(R.id.tvInfoHarga);

        tvUmur = findViewById(R.id.tvUmurDetail);
        tvJenisKelamin = findViewById(R.id.tvJenisKelaminDetail);
        layoutSlot = findViewById(R.id.layoutSlotGroup);

        // 2. Tangkap Data (Gunakan Final agar bisa dipakai di dalam listener/lambda)
        final String nama = getIntent().getStringExtra("nama");
        final long hargaTotal = getIntent().getLongExtra("harga", 0);
        final String berat = getIntent().getStringExtra("berat");
        final String deskripsi = getIntent().getStringExtra("deskripsi");
        final String gambarDetailUrl = getIntent().getStringExtra("gambar_detail");
        final String jenis = getIntent().getStringExtra("jenis");
        final String umur = getIntent().getStringExtra("umur");
        final String jenisKelamin = getIntent().getStringExtra("jenis_kelamin");

        // 🌟 REVISI: Tangkap data slot dinamis (bisa 1, 5, 7, dsb)
        final int slotTerisi = getIntent().getIntExtra("slot_terisi", 0);
        int tempSlotMax = getIntent().getIntExtra("total_slot", 7);
        if (tempSlotMax <= 0) tempSlotMax = 7;
        final int finalSlotMax = tempSlotMax; // Jadikan final untuk Lambda

        // 3. Set Data Dasar
        if (nama != null) tvNama.setText(nama);
        if (berat != null) tvBerat.setText(berat);
        if (umur != null) tvUmur.setText("Umur: " + umur);
        if (jenisKelamin != null) tvJenisKelamin.setText("Kelamin: " + jenisKelamin);
        tvDeskripsi.setText((deskripsi != null && !deskripsi.isEmpty()) ? deskripsi : "Belum ada deskripsi.");

        // Format Rupiah
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        tvHarga.setText(formatRupiah.format(hargaTotal));

        // 4. 🔥 LOGIKA DINAMIS & ANTI-CRASH
        if (jenis != null && jenis.equalsIgnoreCase("kambing")) {
            if (layoutSlot != null) layoutSlot.setVisibility(View.GONE);
            if (btnPatungan != null) btnPatungan.setVisibility(View.VISIBLE);
            if (tvInfoHarga != null) tvInfoHarga.setText("Harga per ekor (Mandiri)");

            // Set Klik untuk Kambing
            if (btnPatungan != null) {
                btnPatungan.setOnClickListener(v -> {
                    Intent i = new Intent(DetailHewanActivity.this, AmbilSlotActivity.class);
                    i.putExtra("nama_hewan", nama);
                    i.putExtra("gambar_detail_sapi", gambarDetailUrl);
                    i.putExtra("slot_terisi", 0);
                    i.putExtra("total_slot", 1);
                    i.putExtra("harga_per_slot", hargaTotal);
                    startActivity(i);
                });
            }
        } else {
            // SAPI: Hitung harga berdasarkan kuota final
            final long hargaPerOrang = hargaTotal / finalSlotMax;

            if (progressPatungan != null) {
                progressPatungan.setMax(finalSlotMax);
                progressPatungan.setProgress(slotTerisi);
            }

            // Update Info UI (Mandiri vs Patungan)
            if (finalSlotMax == 1) {
                if (tvStatusSlot != null) tvStatusSlot.setText("Tipe: Mandiri (1 Sapi Utuh)");
                if (tvInfoHarga != null) tvInfoHarga.setText("Harga 1 Sapi Utuh");
            } else {
                if (tvStatusSlot != null) tvStatusSlot.setText(formatRupiah.format(hargaPerOrang) + " | " + slotTerisi + "/" + finalSlotMax + " Slot");
                if (tvInfoHarga != null) tvInfoHarga.setText("Estimasi per orang (" + finalSlotMax + " Orang)");
            }

            // 🛡️ PROTEKSI TOMBOL SLOT
            if (btnPatungan != null) {
                if (slotTerisi >= finalSlotMax) {
                    btnPatungan.setEnabled(false);
                    btnPatungan.setAlpha(0.3f);
                    if (tvStatusSlot != null) {
                        tvStatusSlot.setText("KUOTA PENUH");
                        tvStatusSlot.setTextColor(Color.RED);
                    }
                } else {
                    btnPatungan.setOnClickListener(v -> {
                        Intent intent = new Intent(DetailHewanActivity.this, AmbilSlotActivity.class);
                        intent.putExtra("nama_hewan", nama);
                        intent.putExtra("gambar_detail_sapi", gambarDetailUrl);
                        intent.putExtra("slot_terisi", slotTerisi);
                        intent.putExtra("total_slot", finalSlotMax);
                        intent.putExtra("harga_per_slot", hargaPerOrang);
                        startActivity(intent);
                    });
                }
            }
        }

        // 5. Load Gambar
        Glide.with(this).load(gambarDetailUrl).placeholder(R.drawable.logo).into(imgHewan);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }
}