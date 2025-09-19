package com.example.homescreen.adapter_packages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homescreen.R;
import com.example.homescreen.model.TopBrand;

import java.util.ArrayList;

public class TopBrandAdapter extends RecyclerView.Adapter<TopBrandAdapter.ViewHolder> {

    ArrayList<TopBrand> brands;
    Context context;

    public TopBrandAdapter(Context context, ArrayList<TopBrand> brands) {
        this.context = context;
        this.brands = brands;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView brandName, brandDesc, brandLoc;
        ImageView brandImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            brandName = itemView.findViewById(R.id.brand_name);
            brandDesc = itemView.findViewById(R.id.brand_description);
            brandLoc = itemView.findViewById(R.id.brand_loc);
            brandImage = itemView.findViewById(R.id.brand_image);

        }
    }

    @NonNull
    @Override
    public TopBrandAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.top_brand_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopBrandAdapter.ViewHolder holder, int position) {
        TopBrand model = brands.get(position);

        holder.brandName.setText(model.getName());
        holder.brandDesc.setText(model.getDescription());
        holder.brandLoc.setText(model.getLoc());
        holder.brandImage.setImageResource(model.getImageUrl());

        // Optional: Add click listener if you want to open brand/shop detail
    }

    @Override
    public int getItemCount() {
        return brands.size();
    }
}