package com.example.qurbancare;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DistribusiActivity extends AppCompatActivity {

    private RecyclerView rvDistribusi;
    private DistribusiAdapter adapter;
    private List<Distribusi> listDistribusi = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button btnTambah, btnAksi, btnLaporan, btnRuteMaps;
    private View btnBack;
    private View btnHitungDistribusi;
    private TextView tvTotalPaket, tvTotalDaging, tvBelumDiambil, tvSudahDiambil;

    private EditText etBeratSapiInput;
    private TextView tvHasilShohibul, tvHasilKantong;
    private double totalBeratSemuaHewan = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distribusi);

        tvTotalPaket = findViewById(R.id.tvTotalPaket);
        tvTotalDaging = findViewById(R.id.tvBerat);
        tvBelumDiambil = findViewById(R.id.tvBelumDiambil);
        tvSudahDiambil = findViewById(R.id.tvSudahDiambil);
        rvDistribusi = findViewById(R.id.rvDistribusi);
        btnTambah = findViewById(R.id.btnTambah);
        btnAksi = findViewById(R.id.btnAksi);
        btnLaporan = findViewById(R.id.btnLaporan);
        btnRuteMaps = findViewById(R.id.btnRuteMaps);
        btnBack = findViewById(R.id.btnBack);

        etBeratSapiInput = findViewById(R.id.etBeratSapiInput);
        btnHitungDistribusi = findViewById(R.id.btnHitungDistribusi);
        tvHasilShohibul = findViewById(R.id.tvHasilShohibul);
        tvHasilKantong = findViewById(R.id.tvHasilKantong);

        if (rvDistribusi != null) {
            rvDistribusi.setLayoutManager(new LinearLayoutManager(this));
            adapter = new DistribusiAdapter(listDistribusi);
            rvDistribusi.setAdapter(adapter);
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> navigasiKeSapa());
        if (btnTambah != null) btnTambah.setOnClickListener(v -> startActivity(new Intent(this, TambahPenerima1Activity.class)));
        if (btnAksi != null) btnAksi.setOnClickListener(v -> showMenuAksi());
        if (btnLaporan != null) btnLaporan.setOnClickListener(v -> buatDanBukaPDF());
        if (btnRuteMaps != null) btnRuteMaps.setOnClickListener(v -> bukaSemuaRuteOtomatis());
        if (btnHitungDistribusi != null) btnHitungDistribusi.setOnClickListener(v -> jalankanKalkulatorOtomatis());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { navigasiKeSapa(); }
        });

        cekRole();
        ambilDataPenerimaRealtime();
        hitungTotalDagingOtomatis();
    }

    private void jalankanKalkulatorOtomatis() {
        if (etBeratSapiInput == null) return;
        String input = etBeratSapiInput.getText().toString().trim();
        double beratDigunakan;
        try {
            beratDigunakan = !input.isEmpty() ? Double.parseDouble(input) : totalBeratSemuaHewan;
            if (beratDigunakan <= 0) {
                Toast.makeText(this, "Masukkan berat atau tunggu data!", Toast.LENGTH_SHORT).show();
                return;
            }
            double karkas = beratDigunakan * 0.35;
            double porsiWarga = karkas / 3;
            double porsiShohibul = karkas / 3;
            int kantong = (int) Math.floor(porsiWarga / 1.0);
            double perShohibul = porsiShohibul / 7;

            if (tvHasilShohibul != null) tvHasilShohibul.setText(String.format("Shohibul: %.1f kg", perShohibul));
            if (tvHasilKantong != null) tvHasilKantong.setText("Warga: " + kantong + " Ktg");
            Toast.makeText(this, "Berhasil dihitung!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Input tidak valid!", Toast.LENGTH_SHORT).show();
        }
    }

    private void ambilDataPenerimaRealtime() {
        db.collection("penerima").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            listDistribusi.clear();
            long totalSeluruhPaket = 0;
            long sudahAmbil = 0;
            long belumAmbil = 0;

            for (DocumentSnapshot doc : value.getDocuments()) {
                Distribusi d = doc.toObject(Distribusi.class);
                if (d != null) {
                    d.setId_penerima(doc.getId());
                    listDistribusi.add(d);

                    // 🌟 REVISI POINT 4: Ambil jumlah paket gelondongan
                    long jml = doc.contains("jumlah_paket") ? doc.getLong("jumlah_paket") : 1;
                    totalSeluruhPaket += jml;

                    boolean status = false;
                    Object s = doc.get("status_ambil");
                    if (s instanceof Boolean) status = (Boolean) s;
                    else if (s instanceof String) status = "true".equalsIgnoreCase((String) s) || "Selesai".equalsIgnoreCase((String) s);

                    if (status) sudahAmbil += jml;
                    else belumAmbil += jml;
                }
            }

            if (tvTotalPaket != null) tvTotalPaket.setText(totalSeluruhPaket + " Paket");
            if (tvSudahDiambil != null) tvSudahDiambil.setText(sudahAmbil + " Paket");
            if (tvBelumDiambil != null) tvBelumDiambil.setText(belumAmbil + " Paket");
            if (adapter != null) adapter.notifyDataSetChanged();
        });
    }

    private void hitungTotalDagingOtomatis() {
        db.collection("qurban").addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            double tempTotal = 0;
            for (DocumentSnapshot doc : value.getDocuments()) {
                Object b = doc.get("berat");
                if (b != null) {
                    String clean = String.valueOf(b).replaceAll("[^0-9]", "");
                    if (!clean.isEmpty()) tempTotal += Double.parseDouble(clean);
                }
            }
            totalBeratSemuaHewan = tempTotal;
            if (tvTotalDaging != null) tvTotalDaging.setText((int)totalBeratSemuaHewan + " kg");
        });
    }

    // --- FITUR PENDUKUNG (TIDAK BERUBAH) ---

    public void bukaPetaKeAlamat(String alamatPenerima) {
        if (alamatPenerima == null || alamatPenerima.isEmpty() || alamatPenerima.equals("-")) return;
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(alamatPenerima));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (Exception e) { Log.e("MAPS_ERROR", "Gagal buka maps"); }
    }

    private void bukaSemuaRuteOtomatis() {
        if (listDistribusi.isEmpty()) return;
        List<String> valid = new ArrayList<>();
        for (Distribusi d : listDistribusi) if (d.getAlamat() != null && !d.getAlamat().equals("-")) valid.add(d.getAlamat());
        if (valid.isEmpty()) return;
        try {
            String tujuan = valid.get(valid.size() - 1);
            StringBuilder wp = new StringBuilder();
            for (int i = 0; i < valid.size() - 1; i++) { if (i > 0) wp.append("|"); wp.append(valid.get(i)); }
            String url = "http://maps.google.com/maps?daddr=" + Uri.encode(tujuan) + "&waypoints=" + Uri.encode(wp.toString());
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) { Log.e("MAPS_ERROR", "Gagal rute"); }
    }

    private void buatDanBukaPDF() {
        if (listDistribusi.isEmpty()) return;
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = doc.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(16f);
        canvas.drawText("LAPORAN DISTRIBUSI QURBAN", 50, 50, paint);
        int y = 100;
        for (Distribusi d : listDistribusi) {
            canvas.drawText((d.getNama() != null ? d.getNama() : "Tanpa Nama") + " - " + d.getKategori(), 50, y, paint);
            y += 25; if (y > 800) break;
        }
        doc.finishPage(page);
        File file = new File(getExternalFilesDir(null), "Laporan_Distribusi.pdf");
        try {
            doc.writeTo(new FileOutputStream(file)); doc.close();
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) { Log.e("PDF_ERROR", "Gagal buat PDF"); }
    }

    private void cekRole() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String role = doc.getString("role");
                    boolean staff = "Admin".equalsIgnoreCase(role) || "Panitia".equalsIgnoreCase(role);
                    if (btnTambah != null) btnTambah.setVisibility(staff ? View.VISIBLE : View.GONE);
                    if (btnAksi != null) btnAksi.setVisibility(staff ? View.VISIBLE : View.GONE);
                    if (btnLaporan != null) btnLaporan.setVisibility(staff ? View.VISIBLE : View.GONE);
                    if (btnRuteMaps != null) btnRuteMaps.setVisibility(staff ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    private void showMenuAksi() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View v = getLayoutInflater().inflate(R.layout.layout_menu_aksi, null);
            dialog.setContentView(v);
            dialog.show();
        } catch (Exception e) { Log.e("UI_ERROR", "Gagal buka menu"); }
    }

    private void navigasiKeSapa() {
        startActivity(new Intent(this, SapaActivity.class));
        finish();
    }
}