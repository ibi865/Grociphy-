package com.example.homescreen.adapter_packages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homescreen.MainActivity;
import com.example.homescreen.R;
import com.example.homescreen.model.Recently_viewed;
import com.example.homescreen.ProductDetailFragment;

import java.util.ArrayList;

public class Recently_viewed_adapter extends RecyclerView.Adapter<Recently_viewed_adapter.ViewHolder> {


    ArrayList<Recently_viewed> people;

    public Recently_viewed_adapter(Context context,ArrayList<Recently_viewed> list){
        this.people=list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView productname,discription,price,qty,unit;
        ConstraintLayout bg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productname=itemView.findViewById(R.id.product_name);
            discription=itemView.findViewById(R.id.description);
            price=itemView.findViewById(R.id.price);
            qty=itemView.findViewById(R.id.qty);
            unit=itemView.findViewById(R.id.unit);
            bg=itemView.findViewById(R.id.recently_layout);



        }
    }

    @NonNull
    @Override
    public Recently_viewed_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.recently_viewed_item,parent,false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull Recently_viewed_adapter.ViewHolder holder, int position) {
        Recently_viewed model = people.get(position);

        holder.productname.setText(model.getName());
        holder.discription.setText(model.getDescription());
        holder.price.setText(model.getPrice());
        holder.qty.setText(model.getQuantity());
        holder.unit.setText(model.getUnit());
        holder.bg.setBackgroundResource(model.getImageUrl());

        // Click Listener to Open ProductDetail Fragment
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                ProductDetailFragment fragment = new ProductDetailFragment();

                // Set data before attaching the fragment
                Bundle bundle = new Bundle();
                bundle.putString("name", model.getName());
                bundle.putString("description", model.getDescription());
                bundle.putString("price", model.getPrice());
                bundle.putInt("imageUrl", model.getBigimgUrl());

                fragment.setArguments(bundle); // Attach data immediately

                View fragmentContainer = activity.findViewById(R.id.product_fragment_container);
                View mainLayout = activity.findViewById(R.id.main);
                View bottomSection = activity.findViewById(R.id.bottom_section);

                if (fragmentContainer != null) {
                    fragmentContainer.setVisibility(View.VISIBLE);
                    mainLayout.setVisibility(View.GONE);
                    bottomSection.setVisibility(View.GONE);

                    // Use .add() instead of .replace() to avoid flickering
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.product_fragment_container, fragment)
                            .commitNow(); // Use commitNow() for instant update
                }
            }
        });

    }



    @Override
    public int getItemCount() {
        return people.size();
    }
}


