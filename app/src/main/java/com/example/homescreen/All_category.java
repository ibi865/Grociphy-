package com.example.homescreen;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homescreen.adapter_packages.All_category_adapter;
import com.example.homescreen.model.All_category_model;

import java.util.ArrayList;

public class All_category extends AppCompatActivity {

    RecyclerView all_category_recyclerview;
    RecyclerView.Adapter all_category_adapter;
    ArrayList<All_category_model> list;
    ImageView back_key;

    ImageView cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_category);

        all_category_recyclerview = findViewById(R.id.all_category_recyclerview);
        back_key=findViewById(R.id.back_key);
        cart=findViewById(R.id.imageView2);

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(All_category.this, Cart_Activity.class);
                startActivity(intent);
                finish(); // Optional: finish current activity
            }
        });

        list = new ArrayList<>();
        // Adding categories
        list.add(new All_category_model(R.drawable.ic_home_fish, "Home Fish"));
        list.add(new All_category_model(R.drawable.ic_home_fruits, "Home Fruits"));
        list.add(new All_category_model(R.drawable.ic_home_meats, "Home Meat"));
        list.add(new All_category_model(R.drawable.ic_home_veggies, "Home Veggies"));
        list.add(new All_category_model(R.drawable.ic_home_fish, "Home Fish"));
        list.add(new All_category_model(R.drawable.ic_home_fruits, "Home Fruits"));
        list.add(new All_category_model(R.drawable.ic_home_meats, "Home Meat"));
        list.add(new All_category_model(R.drawable.ic_home_veggies, "Home Veggies"));

        all_category_recyclerview.setHasFixedSize(true);

        // Using Grid Layout Manager with 3 columns
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        all_category_recyclerview.setLayoutManager(layoutManager);
        all_category_recyclerview.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(16), true));
        all_category_recyclerview.setItemAnimator(new DefaultItemAnimator());

        all_category_adapter = new All_category_adapter(this, list);
        all_category_recyclerview.setAdapter(all_category_adapter);

        back_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(All_category.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    // Grid Spacing Item Decoration
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) { // Top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // Item bottom
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;

                if (position >= spanCount) {
                    outRect.top = spacing; // Item top
                }
            }
        }
    }

    /**
     * Convert dp to pixels.
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
