package com.example.qurbancare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MudhohiAdapter extends RecyclerView.Adapter<MudhohiAdapter.ViewHolder> {
    private List<Mudhohi> list;
    private OnItemClickListener listener;

    // Interface untuk menangani klik dari Activity
    public interface OnItemClickListener {
        void onItemClick(Mudhohi mudhohi);
    }

    public MudhohiAdapter(List<Mudhohi> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Menggunakan layout item yang sudah kamu buat
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transparansi, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mudhohi m = list.get(position);

        if (m != null) {
            // Set Nama dan Detail Hewan
            holder.tvNama.setText(m.getNama() != null ? m.getNama() : "Tanpa Nama");
            holder.tvDetail.setText(m.getJenis_hewan() != null ? m.getJenis_hewan() : "-");

            // Load Gambar kecil di baris list (pake Glide)
            Glide.with(holder.itemView.getContext())
                    .load(m.getGambar())
                    .placeholder(R.drawable.logo) // Pastikan gambar logo ada di folder drawable
                    .error(R.drawable.logo)
                    .centerCrop()
                    .into(holder.imgHewan);

            // 🚀 Aksi saat satu baris kartu diklik
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(m);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDetail;
        ImageView imgHewan;

        public ViewHolder(View itemView) {
            super(itemView);
            // 🔥 ID ini harus sama persis dengan yang ada di item_transparansi.xml
            tvNama = itemView.findViewById(R.id.tvNamaPekurban);
            tvDetail = itemView.findViewById(R.id.tvDetailHewan);
            imgHewan = itemView.findViewById(R.id.imgHewanPekurban);
        }
    }
}