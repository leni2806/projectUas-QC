package com.example.qurbancare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PekurbanAdapter extends RecyclerView.Adapter<PekurbanAdapter.ViewHolder> {

    private List<Pekurban> pekurbanList;

    public PekurbanAdapter(List<Pekurban> pekurbanList) {
        this.pekurbanList = pekurbanList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gunakan layout item yang simpel (bisa pakai android.R.layout.simple_list_item_2 atau buat XML sendiri)
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pekurban pekurban = pekurbanList.get(position);
        holder.text1.setText(pekurban.getNama());
        holder.text2.setText("Berkurban: " + pekurban.getJenis());
    }

    @Override
    public int getItemCount() {
        return pekurbanList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text1, text2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}