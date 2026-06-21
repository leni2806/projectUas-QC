package com.example.qurbancare;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

public class Qurbancare extends AppCompatActivity {
    private ImageView btnSapi, btnKambing, btnBack;
    private TextView tvSapi, tvKambing;
    private RecyclerView rvQurban;
    private FloatingActionButton fabTambah, fabImportQurban;
    private QurbanAdapter adapter;
    private List<Qurban> list = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String jenisAktif = "sapi";

    private CardView cardOptionHewan;
    private RadioGroup rgTipeSapi;
    private View layoutPilihanKuota;
    private CardView btnPlus, btnMinus;
    private EditText etJumlahKuota;

    private final ActivityResultLauncher<Intent> csvPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) prosesImportCSVQurban(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qurbancare);

        btnSapi = findViewById(R.id.btn_sapi);
        btnKambing = findViewById(R.id.btn_kambing);
        btnBack = findViewById(R.id.btn_back);
        tvSapi = findViewById(R.id.tv_sapi);
        tvKambing = findViewById(R.id.tv_kambing);
        rvQurban = findViewById(R.id.rvQurban);
        fabTambah = findViewById(R.id.fabTambah);
        fabImportQurban = findViewById(R.id.fabImportQurban);

        cardOptionHewan = findViewById(R.id.cardOptionHewan);
        rgTipeSapi = findViewById(R.id.rgTipeSapi);
        layoutPilihanKuota = findViewById(R.id.layoutPilihanKuota);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        etJumlahKuota = findViewById(R.id.etJumlahKuota);

        rvQurban.setLayoutManager(new LinearLayoutManager(this));

        // Stepper Logic (+/-)
        if (btnPlus != null) btnPlus.setOnClickListener(v -> {
            int val = Integer.parseInt(etJumlahKuota.getText().toString());
            etJumlahKuota.setText(String.valueOf(val + 1));
        });
        if (btnMinus != null) btnMinus.setOnClickListener(v -> {
            int val = Integer.parseInt(etJumlahKuota.getText().toString());
            if (val > 1) etJumlahKuota.setText(String.valueOf(val - 1));
        });

        // Toggle Mandiri vs Patungan
        if (rgTipeSapi != null) {
            rgTipeSapi.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbMandiri) {
                    layoutPilihanKuota.setVisibility(View.GONE);
                    etJumlahKuota.setText("1");
                } else {
                    layoutPilihanKuota.setVisibility(View.VISIBLE);
                    etJumlahKuota.setText("7");
                }
            });
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        cekAksesAdmin();

        btnSapi.setOnClickListener(v -> {
            jenisAktif = "sapi";
            if (cardOptionHewan != null) cardOptionHewan.setVisibility(View.VISIBLE);
            updateTabUI();
            loadData("sapi");
        });

        btnKambing.setOnClickListener(v -> {
            jenisAktif = "kambing";
            if (cardOptionHewan != null) cardOptionHewan.setVisibility(View.GONE);
            updateTabUI();
            loadData("kambing");
        });

        fabTambah.setOnClickListener(v -> startActivity(new Intent(Qurbancare.this, TambahHewanActivity.class)));
        fabImportQurban.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/*");
            csvPickerLauncher.launch(Intent.createChooser(intent, "Pilih File CSV Qurban"));
        });

        loadData(jenisAktif);
    }

    private void prosesImportCSVQurban(Uri fileUri) {
        Toast.makeText(this, "Sedang mengimport...", Toast.LENGTH_SHORT).show();

        // 🌟 KUNCI: Ambil angka kuota dari Stepper SEBELUM masuk ke thread
        final int kuotaPilihanAdmin = Integer.parseInt(etJumlahKuota.getText().toString());

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                boolean isHeader = true;
                while ((line = reader.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }

                    String[] t = line.split(";", -1); // Sesuaikan separator CSV kamu (;) atau (,)

                    if (t.length >= 7) {
                        String nama = t[0].replace("\"", "").trim();
                        String jenis = t[1].replace("\"", "").trim().toLowerCase();
                        String berat = t[2].replace("\"", "").trim();
                        long harga = Long.parseLong(t[3].replace("\"", "").trim());
                        int st = Integer.parseInt(t[4].replace("\"", "").trim().isEmpty() ? "0" : t[4].replace("\"", "").trim());
                        String jk = t[5].replace("\"", "").trim();
                        String umur = t[6].replace("\"", "").trim();

                        Map<String, Object> map = new HashMap<>();
                        map.put("nama", nama);
                        map.put("jenis", jenis);
                        map.put("berat", berat);
                        map.put("harga", harga);
                        map.put("slot_terisi", st);
                        map.put("jenis_kelamin", jk);
                        map.put("umur", umur + " Tahun");

                        // 🌟 LOGIKA CERDAS: Gunakan angka kuota dari layar
                        if (jenis.contains("kambing") || harga == 0) {
                            map.put("total_slot", 1);
                            if (harga == 0) map.put("slot_terisi", 1);
                        } else {
                            map.put("total_slot", kuotaPilihanAdmin);
                        }

                        map.put("gambar", t.length > 7 ? t[7].replace("\"", "") : "");
                        map.put("gambar_detail", t.length > 8 ? t[8].replace("\"", "") : "");

                        db.collection("qurban").document(nama).set(map);
                    }
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Import Berhasil!", Toast.LENGTH_SHORT).show();
                    loadData(jenisAktif);
                });
            } catch (Exception e) {
                Log.e("CSV_ERROR", e.getMessage());
            }
        }).start();
    }

    private void loadData(String jenis) {
        db.collection("qurban").whereEqualTo("jenis", jenis).get()
                .addOnSuccessListener(snapshots -> {
                    list.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Qurban q = doc.toObject(Qurban.class);
                        if (q != null) list.add(q);
                    }
                    adapter = new QurbanAdapter(list, this, item -> {
                        Intent i = new Intent(Qurbancare.this, DetailHewanActivity.class);
                        i.putExtra("nama", item.getNama());
                        i.putExtra("harga", item.getHarga());
                        i.putExtra("berat", item.getBerat());
                        i.putExtra("jenis", item.getJenis());
                        i.putExtra("umur", item.getUmur());
                        i.putExtra("jenis_kelamin", item.getJenis_kelamin());
                        i.putExtra("slot_terisi", item.getSlot_terisi());
                        i.putExtra("total_slot", item.getTotal_slot());
                        i.putExtra("gambar_detail", item.getGambarDetail());
                        startActivity(i);
                    });
                    rvQurban.setAdapter(adapter);
                });
    }

    private void updateTabUI() {
        if (jenisAktif.equalsIgnoreCase("sapi")) {
            btnSapi.setImageResource(R.drawable.btn_sapi_active);
            btnKambing.setImageResource(R.drawable.btn_kambing_inactive);
            tvSapi.setTextColor(Color.BLACK); tvKambing.setTextColor(Color.GRAY);
        } else {
            btnSapi.setImageResource(R.drawable.btn_sapi_inactive);
            btnKambing.setImageResource(R.drawable.btn_kambing_active);
            tvSapi.setTextColor(Color.GRAY); tvKambing.setTextColor(Color.BLACK);
        }
    }

    private void cekAksesAdmin() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String role = doc.getString("role");
                    boolean isAdmin = "Admin".equalsIgnoreCase(role);
                    fabTambah.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    fabImportQurban.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(jenisAktif);
    }
}