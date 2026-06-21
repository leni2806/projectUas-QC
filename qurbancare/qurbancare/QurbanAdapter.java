package com.example.qurbancare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class QurbanAdapter extends RecyclerView.Adapter<QurbanAdapter.ViewHolder> {
    private List<Qurban> list;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Qurban item);
    }

    public QurbanAdapter(List<Qurban> list, Context context, OnItemClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_qurban, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Qurban q = list.get(position);

        holder.nama.setText(q.getNama());
        holder.berat.setText(q.getBerat());

        // 1. Format Harga ke Rupiah
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.harga.setText(formatRupiah.format(q.getHarga()));

        // 2. LOGIKA REVISI: Sembunyikan Slot Jika Kambing
        // Kita pakai fungsi isUrunan() yang kita buat di model Qurban tadi
        if (q.getJenis() != null && q.getJenis().equalsIgnoreCase("Kambing")) {
            // Jika Kambing: Sembunyikan indikator slot (bulatan angka)
            if (holder.txtSlot != null) holder.txtSlot.setVisibility(View.GONE);
            if (holder.labelSlot != null) holder.labelSlot.setVisibility(View.GONE);
        } else {
            // Jika Sapi/Lainnya: Tampilkan slot seperti biasa
            if (holder.txtSlot != null) {
                holder.txtSlot.setVisibility(View.VISIBLE);
                // Pakai total_slot dari Firebase (Misal Sapi = 7)
                int sisa = q.getTotal_slot() - q.getSlot_terisi();
                holder.txtSlot.setText(String.valueOf(sisa));
            }
            if (holder.labelSlot != null) holder.labelSlot.setVisibility(View.VISIBLE);
        }

        // 3. Load Gambar
        Glide.with(context)
                .load(q.getGambar())
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.img);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(q));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nama, berat, harga, txtSlot, labelSlot;
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nama = itemView.findViewById(R.id.txt_nama);
            berat = itemView.findViewById(R.id.txt_berat);
            harga = itemView.findViewById(R.id.txt_harga);
            img = itemView.findViewById(R.id.img_hewan);
            txtSlot = itemView.findViewById(R.id.txt_slot);

            // Tambahkan ID label "Sisa" atau background bulatannya jika ada
            // labelSlot = itemView.findViewById(R.id.txt_label_sisa);
        }
    }
}