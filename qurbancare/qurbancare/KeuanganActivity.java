package com.example.qurbancare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KeuanganActivity extends AppCompatActivity {

    private RecyclerView rvKeuangan;
    private KeuanganAdapter adapter;
    private List<Keuangan> listKeuanganOriginal = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView tvTotalPeserta, tvSudahBayar, tvBelumBayar, tvTotalDana;
    private ProgressBar progressBelumBayar;
    private String filterAktif = "Semua";

    private View btnTambah, btnUpload, btnExport;

    // 🌟 Format Rupiah Indonesia (Titik ribuan otomatis)
    private NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keuangan);

        tvTotalPeserta = findViewById(R.id.tvTotalPeserta);
        tvSudahBayar = findViewById(R.id.tvSudahBayar);
        tvBelumBayar = findViewById(R.id.tvBelumBayar);
        tvTotalDana = findViewById(R.id.tvTotalDana);
        progressBelumBayar = findViewById(R.id.progressBelumBayar);

        btnTambah = findViewById(R.id.btnTambah);
        btnUpload = findViewById(R.id.btnUpload);
        btnExport = findViewById(R.id.btnExport);

        rvKeuangan = findViewById(R.id.rvKeuangan);
        rvKeuangan.setLayoutManager(new LinearLayoutManager(this));

        setupTombolKlik();
        ambilDataTransaksi();
        cekRoleKeuangan();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void cekRoleKeuangan() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String role = doc.getString("role");
                    int visibility = ("Admin".equalsIgnoreCase(role) || "Panitia".equalsIgnoreCase(role)) ? View.VISIBLE : View.GONE;
                    btnTambah.setVisibility(visibility);
                    btnUpload.setVisibility(visibility);
                    btnExport.setVisibility(visibility);
                }
            });
        }
    }

    private void ambilDataTransaksi() {
        db.collection("transaksi").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                listKeuanganOriginal.clear();
                long totalPemasukan = 0;
                long totalPengeluaran = 0;
                int countTotalPeserta = 0;
                int countSudahBayar = 0;

                for (DocumentSnapshot doc : value.getDocuments()) {
                    Keuangan k = doc.toObject(Keuangan.class);
                    if (k != null) {
                        listKeuanganOriginal.add(k);

                        // 🌟 1. AMBIL ANGKA MURNI (Fix Ratusan Juta & Anti Gagal)
                        long nominal = 0;
                        try {
                            String raw = k.getTotal_harga();
                            if (raw != null) {
                                String clean = raw.replaceAll("[^0-9]", "");
                                // Buang sen ,00 jika ada
                                if (raw.contains(",") && clean.length() > 2) {
                                    clean = clean.substring(0, clean.length() - 2);
                                }
                                nominal = Long.parseLong(clean.isEmpty() ? "0" : clean);
                            }
                        } catch (Exception e) { Log.e("HITUNG", "Gagal konversi"); }

                        // 🌟 2. LOGIKA HITUNG PEMASUKAN & PENGELUARAN
                        // Pastikan field 'tipe' di Firestore isinya "Pemasukan" atau "Pengeluaran"
                        if ("Pemasukan".equalsIgnoreCase(k.getTipe())) {
                            countTotalPeserta++;
                            if ("Lunas".equalsIgnoreCase(k.getStatus())) {
                                countSudahBayar++;
                                totalPemasukan += nominal;
                            }
                        } else if ("Pengeluaran".equalsIgnoreCase(k.getTipe())) {
                            if ("Lunas".equalsIgnoreCase(k.getStatus())) {
                                totalPengeluaran += nominal;
                            }
                        } else {
                            // JAGA-JAGA: Jika field 'tipe' kosong (Data lama/Import Excel)
                            if (k.getEmail() != null && !k.getEmail().isEmpty()) {
                                countTotalPeserta++;
                                if ("Lunas".equalsIgnoreCase(k.getStatus())) {
                                    countSudahBayar++;
                                    totalPemasukan += nominal;
                                }
                            } else {
                                if ("Lunas".equalsIgnoreCase(k.getStatus())) {
                                    totalPengeluaran += nominal;
                                }
                            }
                        }
                    }
                }

                // 🌟 3. KALKULASI DANA BERSIH
                long kasAkhir = totalPemasukan - totalPengeluaran;

                // UPDATE UI DASHBOARD
                tvTotalDana.setText("Rp " + formatter.format(kasAkhir));
                tvTotalPeserta.setText(String.valueOf(countTotalPeserta));
                tvSudahBayar.setText(String.valueOf(countSudahBayar));

                int countBelumBayar = countTotalPeserta - countSudahBayar;
                tvBelumBayar.setText(String.valueOf(countBelumBayar));

                if (countTotalPeserta > 0) {
                    progressBelumBayar.setProgress((countSudahBayar * 100) / countTotalPeserta);
                } else {
                    progressBelumBayar.setProgress(0);
                }
                filterData(filterAktif);
            }
        });
    }

    private void filterData(String kriteria) {
        filterAktif = kriteria;
        List<Keuangan> filtered = new ArrayList<>();
        for (Keuangan k : listKeuanganOriginal) {
            switch (kriteria) {
                case "Semua": filtered.add(k); break;
                case "Lunas": if ("Lunas".equalsIgnoreCase(k.getStatus())) filtered.add(k); break;
                case "Proses": if ("Proses".equalsIgnoreCase(k.getStatus()) || "Pending".equalsIgnoreCase(k.getStatus())) filtered.add(k); break;
                case "Nota": if ("Pengeluaran".equalsIgnoreCase(k.getTipe()) || k.getEmail() == null) filtered.add(k); break;
            }
        }
        adapter = new KeuanganAdapter(filtered);
        rvKeuangan.setAdapter(adapter);
    }

    private void setupTombolKlik() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnTambah.setOnClickListener(v -> startActivity(new Intent(this, PesertaActivity.class)));
        btnUpload.setOnClickListener(v -> startActivity(new Intent(this, UploadNotaActivity.class)));
        btnExport.setOnClickListener(v -> {
            if (listKeuanganOriginal.isEmpty()) Toast.makeText(this, "Data kosong", Toast.LENGTH_SHORT).show();
            else buatLaporanPDF();
        });

        findViewById(R.id.filterSemua).setOnClickListener(v -> filterData("Semua"));
        findViewById(R.id.filterLunas).setOnClickListener(v -> filterData("Lunas"));
        findViewById(R.id.filterBelum).setOnClickListener(v -> filterData("Proses"));
        findViewById(R.id.filterNota).setOnClickListener(v -> filterData("Nota"));
    }

    private void buatLaporanPDF() {
        try {
            File file = new File(getExternalFilesDir(null), "Laporan_Keuangan_QurbanCare.pdf");
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("LAPORAN KEUANGAN QURBANCARE").setBold().setFontSize(18));
            document.add(new Paragraph("Tanggal Cetak: " + new Date().toString() + "\n\n"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 3, 3, 2})).useAllAvailableWidth();
            table.addHeaderCell("Keterangan");
            table.addHeaderCell("Tanggal");
            table.addHeaderCell("Nominal");
            table.addHeaderCell("Status");

            for (Keuangan k : listKeuanganOriginal) {
                table.addCell(k.getNama() != null ? k.getNama() : "-");
                table.addCell(k.getTanggal() != null ? k.getTanggal() : "-");

                String nominalPdf = "0";
                try {
                    String raw = k.getTotal_harga();
                    String clean = raw.replaceAll("[^0-9]", "");
                    if (raw.contains(",") && clean.length() > 2) {
                        clean = clean.substring(0, clean.length() - 2);
                    }
                    long n = Long.parseLong(clean.isEmpty() ? "0" : clean);
                    nominalPdf = "Rp " + formatter.format(n);
                } catch (Exception e) { nominalPdf = k.getTotal_harga(); }

                table.addCell(nominalPdf);
                table.addCell(k.getStatus() != null ? k.getStatus() : "-");
            }

            document.add(table);
            document.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Buka Laporan PDF"));

        } catch (Exception e) {
            Toast.makeText(this, "Gagal cetak PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}