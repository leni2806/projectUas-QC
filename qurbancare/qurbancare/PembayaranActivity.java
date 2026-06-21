package com.example.qurbancare;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PembayaranActivity extends AppCompatActivity {

    private Spinner spMetode;
    private TextView tvTanggal;
    private EditText etNominal;
    private LinearLayout containerUpload, containerInfoRekening;
    private TextView tvNamaBank, tvNoRekening, tvAtasNama;
    private Button btnUpload, btnSelesai;

    private Uri filePath;
    private String metodePilihan = "Cash";
    private String slotDiterima, paketDiterima, hargaDiterima;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembayaran);

        slotDiterima = getIntent().getStringExtra("EXTRA_SLOT");
        paketDiterima = getIntent().getStringExtra("EXTRA_PAKET");
        hargaDiterima = getIntent().getStringExtra("EXTRA_HARGA");

        etNominal = findViewById(R.id.etNominalPembayaran);
        spMetode = findViewById(R.id.spMetodePembayaran);
        tvTanggal = findViewById(R.id.tvTanggalPembayaran);
        containerUpload = findViewById(R.id.containerUploadBukti);

        containerInfoRekening = findViewById(R.id.containerInfoRekening);
        tvNamaBank = findViewById(R.id.tvNamaBankPembayaran);
        tvNoRekening = findViewById(R.id.tvNoRekeningPembayaran);
        tvAtasNama = findViewById(R.id.tvAtasNamaPembayaran);

        btnUpload = findViewById(R.id.btnUploadBukti);
        btnSelesai = findViewById(R.id.btnSelesaiPembayaran);

        if (hargaDiterima != null) {
            try {
                long hargaLong = Long.parseLong(hargaDiterima.replaceAll("[^0-9]", ""));
                NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
                etNominal.setText("Rp " + nf.format(hargaLong));
            } catch (Exception e) {
                etNominal.setText("Rp " + hargaDiterima);
            }
        }

        String[] listMetode = {"Cash", "Transfer Bank"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listMetode);
        spMetode.setAdapter(adapter);

        spMetode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                metodePilihan = listMetode[pos];

                if (metodePilihan.equals("Transfer Bank")) {
                    containerUpload.setVisibility(View.VISIBLE);
                    if (containerInfoRekening != null) containerInfoRekening.setVisibility(View.VISIBLE);
                    ambilRekeningBendaharaOtomatis();
                } else {
                    containerUpload.setVisibility(View.GONE);
                    if (containerInfoRekening != null) containerInfoRekening.setVisibility(View.GONE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        findViewById(R.id.containerTanggal).setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> tvTanggal.setText(d+"/"+(m+1)+"/"+y),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });

        btnSelesai.setOnClickListener(v -> prosesSimpan());

        if (findViewById(R.id.btnBackPembayaran) != null) {
            findViewById(R.id.btnBackPembayaran).setOnClickListener(v -> finish());
        }
    }

    private void ambilRekeningBendaharaOtomatis() {
        db.collection("pengaturan").document("rekening_bendahara")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String bank = documentSnapshot.getString("nama_bank");
                        String norek = documentSnapshot.getString("nomor_rekening");
                        String an = documentSnapshot.getString("atas_nama");

                        if (tvNamaBank != null && bank != null) tvNamaBank.setText(bank);
                        if (tvNoRekening != null && norek != null) tvNoRekening.setText(norek);
                        if (tvAtasNama != null && an != null) tvAtasNama.setText("a.n. " + an);
                    } else {
                        if (tvNamaBank != null) tvNamaBank.setText("Belum Diatur Admin");
                        if (tvNoRekening != null) tvNoRekening.setText("-");
                        if (tvAtasNama != null) tvAtasNama.setText("-");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Gagal memuat info bank", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            btnUpload.setText("Bukti Terpilih ✅");
        }
    }

    private void prosesSimpan() {
        if (tvTanggal.getText().toString().isEmpty()) {
            Toast.makeText(this, "Pilih tanggal dulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Mengirim data transaksi...");
        pd.setCancelable(false);
        pd.show();

        if (metodePilihan.equals("Transfer Bank") && filePath != null) {
            try {
                InputStream is = getContentResolver().openInputStream(filePath);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                updateFirestore(base64Image, pd);
            } catch (Exception e) {
                pd.dismiss();
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
            }
        } else {
            updateFirestore("-", pd);
        }
    }

    private void updateFirestore(String dataBukti, ProgressDialog pd) {
        String uid = FirebaseAuth.getInstance().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            String namaUser = doc.exists() ? doc.getString("nama") : "Pekurban";
            String hargaSimpan = etNominal.getText().toString();

            Map<String, Object> trx = new HashMap<>();
            trx.put("nama", namaUser);
            trx.put("email", email);
            trx.put("metode", metodePilihan);
            trx.put("tanggal", tvTanggal.getText().toString());
            trx.put("bukti", dataBukti);
            trx.put("status", "Pending");
            trx.put("total_harga", hargaSimpan);
            trx.put("paket", paketDiterima != null ? paketDiterima : "Qurban");
            trx.put("slot", slotDiterima != null ? slotDiterima : "1");
            trx.put("tipe", "Pemasukan");

            db.collection("transaksi").add(trx).addOnSuccessListener(a -> {
                pd.dismiss();
                Toast.makeText(this, "Berhasil! Cek status di menu Keuangan.", Toast.LENGTH_LONG).show();
                finish();
            }).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(this, "Gagal simpan transaksi", Toast.LENGTH_SHORT).show();
            });
        });
    }
}