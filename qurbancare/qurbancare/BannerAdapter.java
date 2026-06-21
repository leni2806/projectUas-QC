package com.example.qurbancare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private List<Banner> bannerList;

    public BannerAdapter(List<Banner> bannerList) {
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Pastikan file item_banner.xml sudah ada di folder res/layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Ambil data satu persatu
        Banner banner = bannerList.get(position);

        // Pengaman: Cek jika data banner atau URL gambar tidak null
        if (banner != null && banner.getImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(banner.getImageUrl())
                    .centerCrop() // Agar gambar rapi memenuhi space
                    .placeholder(android.R.drawable.progress_horizontal)
                    .error(android.R.drawable.stat_notify_error) // Jika link gambar rusak
                    .into(holder.imgBanner);
        }
    }

    @Override
    public int getItemCount() {
        // KRITIS: Cek null agar tidak crash saat data Firestore belum ditarik
        return (bannerList != null) ? bannerList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Pastikan di item_banner.xml ada ImageView dengan id imgBanner
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}