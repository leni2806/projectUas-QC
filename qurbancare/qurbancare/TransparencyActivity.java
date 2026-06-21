package com.example.qurbancare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TransparencyActivity extends AppCompatActivity {
    private TextView tvTotalSapi, tvTotalKambing;
    private RecyclerView rvMudhohiPublic;
    private TransparansiAdapter adapter;
    private List<TransparansiModel> listAll = new ArrayList<>();
    private List<TransparansiModel> listFiltered = new ArrayList<>();
    private FirebaseFirestore db;
    private ImageButton btnActionStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparency);

        db = FirebaseFirestore.getInstance();
        tvTotalSapi = findViewById(R.id.tvTotalSapi);
        tvTotalKambing = findViewById(R.id.tvTotalKambing);
        rvMudhohiPublic = findViewById(R.id.rvMudhohiPublic);
        btnActionStart = findViewById(R.id.btnActionStart);

        // Filter Klik (Sapi/Kambing)
        if (tvTotalSapi != null) tvTotalSapi.setOnClickListener(v -> filterData("sapi"));
        if (tvTotalKambing != null) tvTotalKambing.setOnClickListener(v -> filterData("kambing"));

        adapter = new TransparansiAdapter(this, listFiltered);
        rvMudhohiPublic.setLayoutManager(new LinearLayoutManager(this));
        rvMudhohiPublic.setAdapter(adapter);

        // Navigasi ke Detail
        adapter.setOnItemClickListener(model -> {
            Intent intent = new Intent(TransparencyActivity.this, DetailMudhohiActivity.class);
            intent.putExtra("NAMA", model.getNama());
            intent.putExtra("HEWAN", model.getPaket());
            intent.putExtra("GAMBAR", model.getGambar());
            intent.putExtra("GENDER", model.getGender());
            intent.putExtra("UMUR", model.getUmur());
            intent.putExtra("BERAT", model.getBerat());
            intent.putExtra("HARGA", model.getHarga());
            startActivity(intent);
        });

        if (btnActionStart != null) {
            btnActionStart.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }

        loadData();
    }

    private void loadData() {
        // Kita gunakan listener agar data update otomatis (Realtime)

        // 1. Ambil Sapi dari Koleksi TRANSAKSI (Sapi Patungan/Slot)
        db.collection("transaksi").whereEqualTo("status", "Lunas")
                .addSnapshotListener((value, error) -> {
                    if (value == null) return;

                    // Reset list setiap ada perubahan data biar gak duplikat
                    listAll.clear();

                    int countSapi = 0;
                    for (QueryDocumentSnapshot doc : value) {
                        String paket = doc.getString("paket");
                        if (paket != null && paket.toLowerCase().contains("sapi")) {
                            countSapi++;
                            // Ambil detail sapi dari koleksi qurban
                            fetchDetailSapi(doc.getString("nama"), doc.getString("slot"), paket);
                        }
                    }
                    tvTotalSapi.setText(String.valueOf(countSapi));

                    // Setelah ambil sapi, ambil kambing
                    loadKambingManual();
                });
    }

    private void loadKambingManual() {
        // 2. Ambil Kambing dari Koleksi QURBAN (Sesuai Foto Firestore: field 'jenis')
        db.collection("qurban").whereEqualTo("jenis", "kambing")
                .get()
                .addOnSuccessListener(value -> {
                    if (value == null) return;

                    int countKambing = value.size();
                    tvTotalKambing.setText(String.valueOf(countKambing));

                    for (QueryDocumentSnapshot d : value) {
                        // Ambil field sesuai foto: berat, gambar, harga, jenis_kelamin, umur, nama
                        listAll.add(new TransparansiModel(
                                d.getString("nama"), // Nama kambing/pekurban
                                "Manual",
                                d.getString("nama"),
                                d.getString("gambar"),
                                d.getString("jenis_kelamin"),
                                d.getString("umur"),
                                d.getString("berat"),
                                String.valueOf(d.get("harga"))
                        ));
                    }
                    filterData("semua");
                });
    }

    private void fetchDetailSapi(String nP, String sP, String pP) {
        db.collection("qurban").whereEqualTo("nama", pP.toLowerCase().trim()).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot d = querySnapshot.getDocuments().get(0);
                        listAll.add(new TransparansiModel(
                                nP, sP, pP,
                                d.getString("gambar"),
                                d.getString("jenis_kelamin"),
                                d.getString("umur"),
                                d.getString("berat"),
                                String.valueOf(d.get("harga"))
                        ));
                        filterData("semua");
                    }
                });
    }

    private void filterData(String type) {
        listFiltered.clear();
        for (TransparansiModel m : listAll) {
            if (type.equals("semua")) {
                listFiltered.add(m);
            } else if (m.getPaket() != null && m.getPaket().toLowerCase().contains(type)) {
                listFiltered.add(m);
            }
        }
        adapter.notifyDataSetChanged();
    }
}