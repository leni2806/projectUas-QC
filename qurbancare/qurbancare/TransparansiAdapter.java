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
import java.util.List;

public class TransparansiAdapter extends RecyclerView.Adapter<TransparansiAdapter.ViewHolder> {
    private Context context;
    private List<TransparansiModel> list;

    // 🔥 1. Tambahin variabel listener
    private OnItemClickListener listener;

    // 🔥 2. Tambahin Interface buat klik
    public interface OnItemClickListener {
        void onItemClick(TransparansiModel model);
    }

    // 🔥 3. Fungsi buat pasang listener dari Activity
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TransparansiAdapter(Context context, List<TransparansiModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transparansi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransparansiModel data = list.get(position);

        holder.tvNama.setText(data.getNama() + " (" + data.getSlot() + ")");
        holder.tvDetail.setText("Paket: " + data.getPaket());

        Glide.with(context)
                .load(data.getGambar())
                .placeholder(R.drawable.bg)
                .error(R.drawable.bg)
                .into(holder.imgHewan);

        // 🔥 4. Pasang fungsi klik pada item baris
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onItemClick(data);
            }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDetail;
        ImageView imgHewan;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNamaPekurban);
            tvDetail = itemView.findViewById(R.id.tvDetailHewan);
            imgHewan = itemView.findViewById(R.id.imgHewanPekurban);
        }
    }
}