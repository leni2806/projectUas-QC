package com.example.qurbancare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DistribusiAdapter extends RecyclerView.Adapter<DistribusiAdapter.ViewHolder> {
    private List<Distribusi> list;

    public DistribusiAdapter(List<Distribusi> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Pastikan nama file layout benar: item_distribusi
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_distribusi, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (list == null || position >= list.size()) return;

        Distribusi d = list.get(position);
        if (d == null) return;

        // 1. Set data dasar dengan Null Safety
        safeSetText(holder.tvNama, d.getNama());
        safeSetText(holder.tvKategori, d.getKategori());
        safeSetText(holder.tvAlamat, d.getAlamat());
        safeSetText(holder.tvPaket, d.getPaket());
        safeSetText(holder.tvPetugas, d.getPetugas());

        // 2. Menampilkan Berat
        if (holder.tvBerat != null) {
            String b = d.getBerat();
            if (b != null && !b.isEmpty() && !b.equals("-")) {
                String cleanB = b.toLowerCase().contains("kg") ? b : b + " kg";
                holder.tvBerat.setText(cleanB);
            } else {
                holder.tvBerat.setText("0 kg");
            }
        }

        // 3. Logika Status Ambil (UI Label)
        String st = d.getStatus_ambil();
        if (holder.tvStatus != null) {
            if (st == null || st.equalsIgnoreCase("false") || st.equalsIgnoreCase("Belum Ambil") || st.equalsIgnoreCase("Proses")) {
                holder.tvStatus.setText("Belum Ambil");
                try {
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_label);
                } catch (Exception e) { /* Cegah crash jika drawable hilang */ }
            } else {
                holder.tvStatus.setText("Selesai");
                try {
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_selesai);
                } catch (Exception e) { /* Cegah crash jika drawable hilang */ }
            }
        }

        // 4. Logika Klik Baris (Maps) - ANTI CRASH
        holder.itemView.setOnClickListener(v -> {
            String alamatWarga = d.getAlamat();
            Context context = v.getContext();

            if (alamatWarga != null && !alamatWarga.trim().isEmpty() && !alamatWarga.equals("-")) {
                // Gunakan cara yang lebih aman untuk memanggil fungsi di Activity
                if (context instanceof DistribusiActivity) {
                    try {
                        ((DistribusiActivity) context).bukaPetaKeAlamat(alamatWarga);
                    } catch (Exception e) {
                        Toast.makeText(context, "Gagal membuka peta", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(context, "Alamat tidak valid!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fungsi pembantu agar tidak capek nulis if-else null
    private void safeSetText(TextView tv, String text) {
        if (tv != null) {
            tv.setText(text != null && !text.isEmpty() ? text : "-");
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKategori, tvAlamat, tvPaket, tvStatus, tvPetugas, tvBerat;

        public ViewHolder(View itemView) {
            super(itemView);
            // PASTIKAN SEMUA ID INI ADA DI item_distribusi.xml
            tvNama = itemView.findViewById(R.id.tvNama);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvAlamat = itemView.findViewById(R.id.tvAlamat);
            tvPaket = itemView.findViewById(R.id.tvPaket);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPetugas = itemView.findViewById(R.id.tvPetugas);
            tvBerat = itemView.findViewById(R.id.tvBerat);
        }
    }
}