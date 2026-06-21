package com.example.qurbancare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SapaActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private ViewPager2 viewPagerBanner;
    private BannerAdapter bannerAdapter;
    private List<Banner> bannerList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userRole = "Peserta"; // Default role jika data belum narik dari Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sapa);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvGreeting = findViewById(R.id.tvGreeting);
        viewPagerBanner = findViewById(R.id.viewPagerBanner);

        bannerList = new ArrayList<>();
        bannerAdapter = new BannerAdapter(bannerList);
        viewPagerBanner.setAdapter(bannerAdapter);

        setupBannerTransformer();

        // Penting: Ambil data user dulu baru setup tombol
        ambilDataUser();
        loadBannerDariFirestore();
        setupMenuButtons();
        setupPojokInfoKlik();
    }

    private void setupMenuButtons() {
        // --- 1. AKSES UMUM (Semua Role Bisa Masuk) ---

        // Profil
        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Hewan (Peserta wajib bisa masuk untuk ambil slot/bayar)
        findViewById(R.id.btnHewan).setOnClickListener(v ->
                startActivity(new Intent(this, Qurbancare.class)));

        // Panitia (Peserta boleh lihat siapa pengurusnya)
        findViewById(R.id.btnPanitia).setOnClickListener(v ->
                startActivity(new Intent(this, PanitiaActivity.class)));

        // Peserta (Transparansi sesama donatur)
        findViewById(R.id.btnPeserta).setOnClickListener(v ->
                startActivity(new Intent(this, PesertaActivity.class)));

        // Penerima (Melihat daftar mustahiq/warga)
        findViewById(R.id.btnPenerima).setOnClickListener(v ->
                startActivity(new Intent(this, PenerimaActivity.class)));

        // Fitur Chat AI
        View btnChatAi = findViewById(R.id.btnChatAi);
        if (btnChatAi != null) {
            btnChatAi.setOnClickListener(v ->
                    startActivity(new Intent(this, ChatAiActivity.class)));
        }


        // --- 2. AKSES TERBATAS (Hanya Admin & Panitia) ---

        // Distribusi (Data logistik sensitif)
        findViewById(R.id.btnDistribusi).setOnClickListener(v ->
                proteksiAdminPanitia(DistribusiActivity.class));

        // Keuangan (Arus kas utama aplikasi)
        findViewById(R.id.btnPembayaran).setOnClickListener(v ->
                proteksiAdminPanitia(KeuanganActivity.class));

        // Monitor Keramaian (Fitur Keamanan Lapangan)
        View btnScanKeramaian = findViewById(R.id.btnScanKeramaian);
        if (btnScanKeramaian != null) {
            btnScanKeramaian.setOnClickListener(v ->
                    proteksiAdminPanitia(CrowdDetectionActivity.class));
        }
    }

    // Fungsi "Satpam" untuk memfilter akses halaman sensitif
    private void proteksiAdminPanitia(Class<?> tujuan) {
        if (userRole != null && (userRole.equalsIgnoreCase("Admin") || userRole.equalsIgnoreCase("Panitia"))) {
            startActivity(new Intent(this, tujuan));
        } else {
            Toast.makeText(this, "Maaf, menu ini hanya bisa diakses Panitia/Admin", Toast.LENGTH_SHORT).show();
        }
    }

    private void ambilDataUser() {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nama = doc.getString("nama");
                            userRole = doc.getString("role");

                            if (nama != null && tvGreeting != null) {
                                tvGreeting.setText("Assalamualaikum, " + nama + "!");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal memuat profil user", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // --- Sisanya (Banner & Link) Tetap Sama ---

    private void setupPojokInfoKlik() {
        findViewById(R.id.cardApaItuQurban).setOnClickListener(v ->
                bukaLink("https://share.google/Dy9UlvWR3UYNUeqNn"));
        findViewById(R.id.cardTipsHewan).setOnClickListener(v ->
                bukaLink("https://share.google/ZqYuPLdIoEnwnIpy2"));
    }

    private void bukaLink(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membuka link", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBannerDariFirestore() {
        db.collection("banners").get().addOnSuccessListener(snapshots -> {
            bannerList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                bannerList.add(doc.toObject(Banner.class));
            }
            bannerAdapter.notifyDataSetChanged();
        });
    }

    private void setupBannerTransformer() {
        if (viewPagerBanner == null) return;
        viewPagerBanner.setClipToPadding(false);
        viewPagerBanner.setClipChildren(false);
        viewPagerBanner.setOffscreenPageLimit(3);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        viewPagerBanner.setPageTransformer(transformer);
    }
}