package com.example.qurbancare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PesertaActivity extends AppCompatActivity {
    private RecyclerView rvPeserta;
    private PesertaAdapter adapter;
    private List<Peserta> pesertaList = new ArrayList<>();
    private EditText etSearch;
    private FloatingActionButton fabTambah;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userRole = "Peserta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peserta);

        rvPeserta = findViewById(R.id.rvPeserta);
        etSearch = findViewById(R.id.etSearchPeserta);
        fabTambah = findViewById(R.id.fabTambah);

        rvPeserta.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PesertaAdapter(pesertaList, item -> {
            if ("Admin".equalsIgnoreCase(userRole) || "Panitia".equalsIgnoreCase(userRole)) {
                showDialogValidasi(item);
            } else {
                Toast.makeText(this, "Hanya Panitia yang dapat memvalidasi pembayaran", Toast.LENGTH_SHORT).show();
            }
        });
        rvPeserta.setAdapter(adapter);

        findViewById(R.id.btnBackPeserta).setOnClickListener(v -> finish());

        ambilRoleDanSetUI();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataPeserta();
    }

    private void ambilRoleDanSetUI() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    userRole = doc.getString("role");
                    if ("Admin".equalsIgnoreCase(userRole)) {
                        fabTambah.setVisibility(View.VISIBLE);
                        fabTambah.setOnClickListener(v -> {
                            startActivity(new Intent(PesertaActivity.this, TambahPesertaActivity.class));
                        });
                    } else {
                        fabTambah.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void loadDataPeserta() {
        db.collection("transaksi").get().addOnSuccessListener(snapshots -> {
            pesertaList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                String tipe = doc.getString("tipe");
                String email = doc.getString("email");

                if (!"Pengeluaran".equalsIgnoreCase(tipe) && email != null && !email.isEmpty()) {
                    Peserta p = doc.toObject(Peserta.class);
                    p.setId(doc.getId());
                    pesertaList.add(p);
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Gagal memuat data!", Toast.LENGTH_SHORT).show());
    }

    private void showDialogValidasi(Peserta p) {
        if ("Lunas".equalsIgnoreCase(p.getStatus())) {
            Toast.makeText(this, "Peserta ini sudah lunas.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Validasi Pembayaran")
                .setMessage("Ubah status " + p.getNama() + " menjadi Lunas?")
                .setPositiveButton("Ya, Lunas", (d, w) -> {
                    db.collection("transaksi").document(p.getId())
                            .update("status", "Lunas")
                            .addOnSuccessListener(a -> {

                                // 🌟 REVISI POIN 5: Cek apakah ini Bapak-bapak (WA) atau Milenial (Email)
                                if (p.getEmail() != null && p.getEmail().contains("@qurbancare.com")) {
                                    // Trik: Ekstrak nomor WA dari email bayangan
                                    String nomorHPMurni = p.getEmail().replace("@qurbancare.com", "");
                                    kirimWhatsAppOtomatis(nomorHPMurni, p.getNama(), p.getPaket());
                                } else {
                                    // Kirim via EmailJS
                                    kirimEmailOtomatis(p.getEmail(), p.getNama(), p.getPaket());
                                }

                                if (p.getPaket() != null && !p.getPaket().equals("-")) {
                                    updateProgresHewan(p.getPaket());
                                } else {
                                    Toast.makeText(this, "Lunas!", Toast.LENGTH_SHORT).show();
                                    loadDataPeserta();
                                }
                            });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // 🌟 METHOD BARU: Kirim Notifikasi via WA
    private void kirimWhatsAppOtomatis(String nomorWA, String namaPeserta, String namaHewan) {
        String formattedNo = nomorWA;
        if (formattedNo.startsWith("0")) {
            formattedNo = "62" + formattedNo.substring(1);
        }

        String pesan = "Assalamu’alaikum Warahmatullahi Wabarakatuh, *" + namaPeserta + "*\n\n" +
                "Alhamdulillah, kami menginformasikan bahwa pembayaran Qurban Anda untuk: *" + namaHewan + "* telah *DIVALIDASI* oleh Admin.\n\n" +
                "Status Pembayaran: *LUNAS*\n\n" +
                "Jazakumullah Khairan Katsiran (Semoga Allah membalas kebaikan Anda dengan kebaikan yang banyak) telah mempercayakan ibadah Qurban Anda melalui *QurbanCare*.\n\n" +
                "Semoga ibadah Qurban ini menjadi amal jariyah dan membawa keberkahan bagi Anda serta keluarga. Aamiin Allahumma Aamiin.\n\n" +
                "Wassalamu’alaikum Warahmatullahi Wabarakatuh,\n\n" +
                "Salam, *Admin QurbanCare*";

        try {
            String url = "https://api.whatsapp.com/send?phone=" + formattedNo + "&text=" + Uri.encode(pesan);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membuka WhatsApp!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgresHewan(String namaPaket) {
        String paketFix = namaPaket.toLowerCase().trim();
        db.collection("qurban")
                .whereEqualTo("nama", paketFix)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String docId = doc.getId();
                        Long slotTerisi = doc.getLong("slot_terisi");

                        if (slotTerisi != null) {
                            db.collection("qurban").document(docId)
                                    .update("slot_terisi", slotTerisi + 1)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Pembayaran Valid & Progres Naik!", Toast.LENGTH_SHORT).show();
                                        loadDataPeserta();
                                    });
                        }
                    } else {
                        loadDataPeserta();
                    }
                });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s.toString().toLowerCase();
                List<Peserta> filtered = new ArrayList<>();
                for (Peserta p : pesertaList) {
                    if (p.getNama().toLowerCase().contains(key)) {
                        filtered.add(p);
                    }
                }
                adapter.updateList(filtered);
            }
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void kirimEmailOtomatis(String emailTujuan, String namaPeserta, String namaHewan) {
        if (emailTujuan == null || emailTujuan.isEmpty()) return;

        OkHttpClient client = new OkHttpClient();
        String templateId = "template_f5rntca";
        String publicKey = "cT0dzrXNbzb1lz8q1";

        String jsonBody = "{"
                + "\"service_id\": \"service_klt99hq\","
                + "\"template_id\": \"" + templateId + "\","
                + "\"user_id\": \"" + publicKey + "\","
                + "\"template_params\": {"
                + "    \"to_email\": \"" + emailTujuan + "\","
                + "    \"nama_peserta\": \"" + namaPeserta + "\","
                + "    \"nama_hewan\": \"" + namaHewan + "\""
                + "}"
                + "}";

        RequestBody body = RequestBody.create(
                jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://api.emailjs.com/api/v1.0/email/send")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EMAIL_ERROR", "Gagal: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->
                            Toast.makeText(getApplicationContext(), "Email Notifikasi Terkirim!", Toast.LENGTH_SHORT).show()
                    );
                }
                response.close();
            }
        });
    }
}