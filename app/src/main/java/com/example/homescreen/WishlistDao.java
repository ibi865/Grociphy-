// WishlistDao.java
package com.example.homescreen;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.homescreen.model.Wishlist;

import java.util.List;

@Dao
public interface WishlistDao {

    @Insert
    void insert(Wishlist item);

    @Delete
    void delete(Wishlist item);

    @Query("SELECT * FROM wishlist_items")
    LiveData<List<Wishlist>> getAllItems();

    @Query("DELETE FROM wishlist_items")
    void clearWishlist();

    @Query("SELECT * FROM wishlist_items WHERE productName = :productName LIMIT 1")
    Wishlist getWishlistItemByName(String productName);
}
