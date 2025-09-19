package com.example.homescreen.adapter_packages;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homescreen.R;
import com.example.homescreen.model.Discounted_products;

import java.util.ArrayList;

public class discounted_adapter extends RecyclerView.Adapter<discounted_adapter.ViewHolder> {

    private ArrayList<Discounted_products> list;
    private Context context;

    // Correct Constructor
    public discounted_adapter(Context context, ArrayList<Discounted_products> products) {
        this.context = context;
        this.list = products;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.discounted_image);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.discounted_row_tems, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Corrected Image Binding
        holder.img.setImageResource(list.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
