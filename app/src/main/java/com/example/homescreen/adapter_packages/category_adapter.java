package com.example.homescreen.adapter_packages;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homescreen.Product_Category;
import com.example.homescreen.R;
import com.example.homescreen.model.Category;

import java.util.ArrayList;

public class category_adapter extends RecyclerView.Adapter<category_adapter.ViewHolder> {

    ArrayList<Category> category_list;
    public category_adapter(Context context , ArrayList<Category> people){
        category_list=people;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = category_list.get(position);
        holder.categoryimg.setImageResource(category.getId());

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, Product_Category.class);

            // Optional: pass data like category name or id
            // intent.putExtra("categoryId", category.getId());

            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return category_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView categoryimg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryimg=itemView.findViewById(R.id.category_image);


        }
    }

}
