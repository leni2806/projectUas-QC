package com.example.qurbancare;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.text.NumberFormat;
import java.util.Locale;

public class DetailMudhohiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_mudhohi);

        // 1. Inisialisasi View
        ImageView imgHewan = findViewById(R.id.detailGambar);
        TextView tvNama = findViewById(R.id.detailNama);
        TextView tvHewan = findViewById(R.id.detailHewan);
        TextView tvGender = findViewById(R.id.detailGender);
        TextView tvUmur = findViewById(R.id.detailUmur);
        TextView tvHarga = findViewById(R.id.detailHarga);
        TextView tvBerat = findViewById(R.id.detailKeterangan);
        Button btnClose = findViewById(R.id.btnClose);

        // 2. Tangkap data dari Intent
        String nama = getIntent().getStringExtra("NAMA");
        String hewan = getIntent().getStringExtra("HEWAN");
        String gender = getIntent().getStringExtra("GENDER");
        String umur = getIntent().getStringExtra("UMUR");
        String harga = getIntent().getStringExtra("HARGA");
        String gambar = getIntent().getStringExtra("GAMBAR");
        String berat = getIntent().getStringExtra("BERAT");

        // 3. Set Data Tekstual
        tvNama.setText(nama != null ? nama : "-");
        tvHewan.setText(hewan != null ? hewan : "-");
        tvGender.setText(gender != null ? gender : "-");
        tvUmur.setText(umur != null ? umur : "-");
        tvBerat.setText(berat != null ? "Berat estimasi: " + berat + " kg" : "-");

        // 4. Format Harga ke Rupiah
        if (harga != null) {
            try {
                double hargaDouble = Double.parseDouble(harga.replaceAll("[^0-9]", ""));
                Locale localeID = new Locale("in", "ID");
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                tvHarga.setText(formatRupiah.format(hargaDouble));
            } catch (Exception e) {
                tvHarga.setText("Rp " + harga);
            }
        }

        // 5. 🔥 LOAD GAMBAR FINAL (ANTI BLUR)
        // Menggunakan fitCenter di Glide agar sejalan dengan XML
        Glide.with(this)
                .load(gambar)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache data asli
                        .fitCenter()) // Menjaga ketajaman resolusi asli
                .into(imgHewan);

        btnClose.setOnClickListener(v -> finish());
    }
}