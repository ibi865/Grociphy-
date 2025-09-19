// AppDatabase.java
package com.example.homescreen;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.homescreen.model.Cart_Model;
import com.example.homescreen.model.Wishlist;

@Database(entities = {Cart_Model.class, Wishlist.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CartDao cartDao();
    public abstract WishlistDao wishlistDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "cart_database")
                            .fallbackToDestructiveMigration() // This will wipe and recreate on version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}