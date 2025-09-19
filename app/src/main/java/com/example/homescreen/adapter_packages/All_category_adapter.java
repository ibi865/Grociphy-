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
import com.example.homescreen.model.All_category_model;

import java.util.ArrayList;

public class All_category_adapter extends RecyclerView.Adapter<All_category_adapter.ViewHolder> {

    ArrayList<All_category_model> category_list;
    Context context;
    public All_category_adapter(Context context, ArrayList<All_category_model> people) {
        this.context = context;
        category_list = people;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_row_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        All_category_model model = category_list.get(position);
        holder.categoryimg.setImageResource(model.getId());

        // When a category is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Product_Category.class);
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
