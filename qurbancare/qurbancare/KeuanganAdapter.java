package com.example.qurbancare;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class KeuanganAdapter extends RecyclerView.Adapter<KeuanganAdapter.ViewHolder> {
    private List<Keuangan> list;

    public KeuanganAdapter(List<Keuangan> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_keuangan, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Keuangan k = list.get(position);

        // 1. Set Nama dan Slot
        if (k.getSlot() != null) {
            holder.tvNama.setText(k.getNama() + " (" + k.getSlot() + ")");
        } else {
            holder.tvNama.setText(k.getNama());
        }

        holder.tvTanggal.setText(k.getTanggal() + " | " + k.getMetode());

        // --- 🌟 LOGIKA ANTI RATUSAN JUTA (FIX FINAL) ---
        try {
            String raw = k.getTotal_harga();

            // A. Ambil digit angkanya saja
            String clean = raw != null ? raw.replaceAll("[^0-9]", "") : "0";

            // B. POTONG BUNTUT: Jika ada koma (",00"), buang 2 digit nol palsu di belakang
            if (raw != null && raw.contains(",") && clean.length() > 2) {
                clean = clean.substring(0, clean.length() - 2);
            }

            long hargaLong = Long.parseLong(clean.isEmpty() ? "0" : clean);

            // C. Format ke Rupiah Indonesia yang cantik
            NumberFormat formatIndo = NumberFormat.getInstance(new Locale("id", "ID"));
            holder.tvNominal.setText("Rp " + formatIndo.format(hargaLong));

        } catch (Exception e) {
            // Kalau gagal, tampilkan teks aslinya aja biar gak kosong
            holder.tvNominal.setText(k.getTotal_harga());
        }

        // --- 2. LOGIKA BUKTI FOTO (BASE64) ---
        if (k.getBukti() != null && !k.getBukti().equals("-") && !k.getBukti().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(k.getBukti(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                holder.ivBukti.setImageBitmap(decodedByte);
                holder.ivBukti.setVisibility(View.VISIBLE);

                // Klik gambar untuk Zoom Fullscreen
                holder.ivBukti.setOnClickListener(v -> {
                    Dialog dialog = new Dialog(v.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    ImageView ivFull = new ImageView(v.getContext());
                    ivFull.setImageBitmap(decodedByte);
                    ivFull.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    ivFull.setBackgroundColor(Color.BLACK);
                    dialog.setContentView(ivFull);
                    ivFull.setOnClickListener(view -> dialog.dismiss());
                    dialog.show();
                });
            } catch (Exception e) {
                holder.ivBukti.setVisibility(View.GONE);
            }
        } else {
            holder.ivBukti.setVisibility(View.GONE);
            holder.ivBukti.setOnClickListener(v ->
                    Toast.makeText(v.getContext(), "Metode Cash: Tidak ada bukti foto", Toast.LENGTH_SHORT).show()
            );
        }

        // 3. Logika Warna Status
        if ("Lunas".equalsIgnoreCase(k.getStatus())) {
            holder.viewIndikator.setBackgroundColor(Color.parseColor("#2D5A27"));
            holder.tvStatus.setText("Lunas");
            holder.tvStatus.setTextColor(Color.parseColor("#2D5A27"));
        } else {
            holder.viewIndikator.setBackgroundColor(Color.parseColor("#D4A373"));
            holder.tvStatus.setText(k.getStatus());
            holder.tvStatus.setTextColor(Color.parseColor("#D4A373"));
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvTanggal, tvNominal, tvStatus;
        ImageView ivBukti;
        View viewIndikator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNamaTransaksi);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            tvNominal = itemView.findViewById(R.id.tvNominal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            viewIndikator = itemView.findViewById(R.id.viewIndikator);
            ivBukti = itemView.findViewById(R.id.ivBuktiNota);
        }
    }
}