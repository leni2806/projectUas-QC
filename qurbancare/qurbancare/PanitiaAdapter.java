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

public class PanitiaAdapter extends RecyclerView.Adapter<PanitiaAdapter.ViewHolder> {

    private List<Panitia> panitiaList;

    // Tambahkan context jika diperlukan, tapi Glide bisa pakai holder.itemView
    public PanitiaAdapter(List<Panitia> panitiaList, PanitiaActivity activity) {
        this.panitiaList = panitiaList;
    }

    public void updateList(List<Panitia> newList) {
        this.panitiaList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Pastikan nama layout XML kamu adalah item_panitia
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panitia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Panitia panitia = panitiaList.get(position);

        holder.tvNama.setText(panitia.getNama());
        holder.tvEmail.setText(panitia.getEmail());
        holder.tvRole.setText(panitia.getRole());

        // 🔥 LOGIKA REVISI DOSEN: Menampilkan Bagian/Tugas
        if (panitia.getBagian() != null && !panitia.getBagian().isEmpty()) {
            holder.tvBagian.setText(panitia.getBagian());
            holder.tvBagian.setVisibility(View.VISIBLE);
        } else {
            // Jika bagian kosong, tampilkan teks default atau sembunyikan
            holder.tvBagian.setText("Anggota Panitia");
        }

        // Load Foto Panitia pakai Glide
        Glide.with(holder.itemView.getContext())
                .load(panitia.getFotoUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_profil)
                .error(R.drawable.ic_profil)
                .into(holder.imgPanitia);
    }

    @Override
    public int getItemCount() {
        return (panitiaList != null) ? panitiaList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPanitia;
        TextView tvNama, tvEmail, tvRole, tvBagian; // 🔥 Tambahkan tvBagian

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPanitia = itemView.findViewById(R.id.imgPanitia);
            tvNama = itemView.findViewById(R.id.tvNamaPanitia);
            tvEmail = itemView.findViewById(R.id.tvEmailPanitia);
            tvRole = itemView.findViewById(R.id.tvRolePanitia);
            // 🔥 Hubungkan ke ID yang ada di layout item_panitia.xml
            tvBagian = itemView.findViewById(R.id.tvBagianPanitia);
        }
    }
}