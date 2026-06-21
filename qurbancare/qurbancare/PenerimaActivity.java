package com.example.qurbancare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PenerimaActivity extends AppCompatActivity {
    private RecyclerView rvPenerima;
    private PenerimaAdapter adapter;
    private List<Penerima> listPenerima = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvTotal, tvWarga, tvDhuafa, tvPesantren;
    private EditText etSearch;
    private FloatingActionButton fabScan, fabImportExcel;
    private View cardKupon;
    private String userRole = "Peserta";

    private final ActivityResultLauncher<Intent> csvPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) prosesImportCSV(uri);
                }
            }
    );

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) prosesKonfirmasiQR(result.getContents());
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penerima);

        tvTotal = findViewById(R.id.tvTotalDashboard);
        tvWarga = findViewById(R.id.tvWargaDashboard);
        tvDhuafa = findViewById(R.id.tvDhuafaDashboard);
        tvPesantren = findViewById(R.id.tvPesantrenDashboard);
        etSearch = findViewById(R.id.etCariNamaPenerima);
        fabScan = findViewById(R.id.fabScan);
        cardKupon = findViewById(R.id.cardKupon);
        fabImportExcel = findViewById(R.id.fabImportExcel);

        rvPenerima = findViewById(R.id.rvPenerima);
        rvPenerima.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PenerimaAdapter(this, listPenerima);
        rvPenerima.setAdapter(adapter);

        muatDataDanHitung();
        cekRolePenerima();

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterData(s.toString()); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        fabScan.setOnClickListener(v -> {
            ScanOptions opt = new ScanOptions();
            opt.setPrompt("Scan Kupon Warga");
            barcodeLauncher.launch(opt);
        });

        fabImportExcel.setOnClickListener(v -> openFilePicker());
        findViewById(R.id.btnBackPenerima).setOnClickListener(v -> finish());
    }

    private void cekRolePenerima() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    userRole = doc.getString("role");
                    if ("Admin".equalsIgnoreCase(userRole) || "Panitia".equalsIgnoreCase(userRole)) {
                        fabScan.setVisibility(View.VISIBLE);
                        fabImportExcel.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void muatDataDanHitung() {
        db.collection("penerima").addSnapshotListener((value, error) -> {
            if (value != null) {
                listPenerima.clear();
                int w = 0, d = 0, p = 0;
                for (QueryDocumentSnapshot doc : value) {
                    Penerima item = doc.toObject(Penerima.class);
                    item.setId(doc.getId());
                    listPenerima.add(item);
                    String kat = item.getKategori();
                    if (kat != null) {
                        if (kat.equalsIgnoreCase("Warga")) w++;
                        else if (kat.equalsIgnoreCase("Dhuafa")) d++;
                        else if (kat.equalsIgnoreCase("Pesantren") || kat.equalsIgnoreCase("Yayasan")) p++;
                    }
                }
                tvTotal.setText(String.valueOf(listPenerima.size()));
                tvWarga.setText(String.valueOf(w));
                tvDhuafa.setText(String.valueOf(d));
                tvPesantren.setText(String.valueOf(p));
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void prosesKonfirmasiQR(String idQR) {
        db.collection("penerima").document(idQR.trim()).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                boolean sudahAmbil = doc.getBoolean("status_ambil") != null && doc.getBoolean("status_ambil");
                String nama = doc.getString("nama");

                // 🌟 REVISI POINT 4: Ambil jumlah paket (Gelondongan)
                long jumlah = doc.contains("jumlah_paket") ? doc.getLong("jumlah_paket") : 1;

                if (sudahAmbil) {
                    Toast.makeText(this, "⚠️ GAGAL! Kupon " + nama + " sudah digunakan.", Toast.LENGTH_LONG).show();
                } else {
                    db.collection("penerima").document(idQR.trim()).update("status_ambil", true)
                            .addOnSuccessListener(a -> {
                                String msg = (jumlah > 1) ? "✅ BERHASIL! Berikan " + jumlah + " paket ke " + nama : "✅ BERHASIL! Berikan 1 paket ke " + nama;
                                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            });
                }
            } else {
                Toast.makeText(this, "❌ Kupon Tidak Terdaftar!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        csvPickerLauncher.launch(Intent.createChooser(intent, "Pilih File CSV"));
    }

    private void prosesImportCSV(Uri fileUri) {
        Toast.makeText(this, "Mengimpor data...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(fileUri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                boolean isHeader = true;
                int count = 0;

                while ((line = reader.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }
                    String[] tokens = line.split("[,;]", -1);
                    if (tokens.length >= 2) {
                        String id = tokens[0].replace("\"", "").trim();
                        String nama = tokens[1].replace("\"", "").trim();

                        Map<String, Object> data = new HashMap<>();
                        data.put("id_penerima", id);
                        data.put("nama", nama);
                        data.put("alamat", tokens.length > 2 ? tokens[2].replace("\"", "").trim() : "");
                        data.put("kategori", tokens.length > 3 ? tokens[3].replace("\"", "").trim() : "Warga");
                        data.put("telepon", tokens.length > 4 ? tokens[4].replace("\"", "").trim() : "");
                        data.put("status_ambil", false);

                        // 🌟 REVISI POINT 4: Cek kolom ke-6 untuk jumlah_paket
                        int jml = 1;
                        if (tokens.length > 6 && !tokens[6].isEmpty()) {
                            try { jml = Integer.parseInt(tokens[6].replace("\"", "").trim()); } catch (Exception e) {}
                        }
                        data.put("jumlah_paket", jml);

                        db.collection("penerima").document(id).set(data);
                        count++;
                    }
                }
                int finalCount = count;
                runOnUiThread(() -> Toast.makeText(this, "✅ Sukses Impor " + finalCount + " data!", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void filterData(String query) {
        List<Penerima> filtered = new ArrayList<>();
        for (Penerima p : listPenerima) {
            if (p.getNama().toLowerCase().contains(query.toLowerCase()) || p.getKategori().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }
}