package com.example.qurbancare;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PesertaAdapter extends RecyclerView.Adapter<PesertaAdapter.ViewHolder> {
    private List<Peserta> list;
    private OnItemClickListener listener;

    // Interface untuk mendeteksi klik (untuk validasi Admin/Panitia)
    public interface OnItemClickListener {
        void onItemClick(Peserta peserta);
    }

    public PesertaAdapter(List<Peserta> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_peserta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Peserta p = list.get(position);

        // Mengisi data dasar
        holder.tvNama.setText(p.getNama());
        holder.tvEmail.setText(p.getEmail());

        // Menggabungkan Paket dan Slot (Contoh: Sapi ke 1 / Slot ke-6)
        String infoPaket = p.getPaket() + " / " + p.getSlot();
        holder.tvPaket.setText(infoPaket);

        // LOGIKA STATUS: Mengatur Teks, Warna, dan Background secara otomatis
        if ("Lunas".equalsIgnoreCase(p.getStatus())) {
            holder.tvStatus.setText("✔ Lunas");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32")); // Hijau Tua
            holder.tvStatus.setBackgroundResource(R.drawable.lunas);  // Pakai drawable lunas.xml
        } else {
            holder.tvStatus.setText("✘ Pay");
            holder.tvStatus.setTextColor(Color.parseColor("#C62828")); // Merah Tua
            holder.tvStatus.setBackgroundResource(R.drawable.pay);    // Pakai drawable pay.xml
        }

        // Listener Klik: Supaya kartu bisa diklik untuk ubah status
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Fungsi untuk memperbarui daftar saat pencarian
    public void updateList(List<Peserta> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvEmail, tvPaket, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Pastikan ID ini sama dengan yang ada di item_peserta.xml
            tvNama = itemView.findViewById(R.id.tvNamaPeserta);
            tvEmail = itemView.findViewById(R.id.tvEmailPeserta);
            tvPaket = itemView.findViewById(R.id.tvPaketPeserta);
            tvStatus = itemView.findViewById(R.id.tvStatusPeserta);
        }
    }
}