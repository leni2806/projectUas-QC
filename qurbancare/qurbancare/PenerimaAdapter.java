package com.example.qurbancare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class PenerimaAdapter extends RecyclerView.Adapter<PenerimaAdapter.ViewHolder> {
    private Context context;
    private List<Penerima> list;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PenerimaAdapter(Context context, List<Penerima> list) {
        this.context = context;
        this.list = list;
    }

    // --- FITUR SEARCH & FILTER (PENTING!) ---
    // Method ini yang dipanggil dari PenerimaActivity.java
    public void updateList(List<Penerima> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_penerima, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Penerima p = list.get(position);

        holder.tvNama.setText(p.getNama());
        holder.tvKat.setText(p.getKategori());
        holder.tvDet.setText(p.getAlamat() + " | " + p.getTelepon());
        holder.imgAv.setImageResource(R.drawable.avatar_boy);

        // Update status tampilan
        if (p.isStatus_ambil()) {
            holder.tvStat.setText("Sudah diambil");
            holder.tvStat.setBackgroundResource(R.drawable.bg_status_hijau);
        } else {
            holder.tvStat.setText("Belum diambil");
            holder.tvStat.setBackgroundResource(R.drawable.bg_status_kuning);
        }

        // Tombol Hapus dengan Konfirmasi
        holder.btnHap.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Hapus Data")
                    .setMessage("Yakin hapus " + p.getNama() + "?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        if (p.getId() != null) {
                            db.collection("penerima").document(p.getId()).delete()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Data berhasil dihapus", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("Batal", null).show();
        });

        // Tombol Edit
        holder.btnEd.setOnClickListener(v -> Toast.makeText(context, "Edit: " + p.getNama(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAv;
        TextView tvNama, tvKat, tvDet, tvStat;
        Button btnEd, btnHap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAv = itemView.findViewById(R.id.imgAvPenerima);
            tvNama = itemView.findViewById(R.id.tvNamaPenerima);
            tvKat = itemView.findViewById(R.id.tvKatPenerima);
            tvDet = itemView.findViewById(R.id.tvDetPenerima);
            tvStat = itemView.findViewById(R.id.tvStatAmbil);
            btnEd = itemView.findViewById(R.id.btnEdPenerima);
            btnHap = itemView.findViewById(R.id.btnHapPenerima);
        }
    }
}