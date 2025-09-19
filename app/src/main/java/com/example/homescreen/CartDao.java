// CartDao.java
package com.example.homescreen;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.homescreen.model.Cart_Model;

import java.util.List;

@Dao
public interface CartDao {


    @Insert
    void insert(Cart_Model item);

    @Update
    void update(Cart_Model item);

    @Delete
    void delete(Cart_Model item);

    @Query("SELECT * FROM cart_items")
    LiveData<List<Cart_Model>> getAllItems();

    @Query("DELETE FROM cart_items")
    void clearCart();

    @Query("SELECT SUM(totalPrice) FROM cart_items")
    LiveData<Double> getSubtotal();

    @Query("SELECT * FROM cart_items WHERE productName = :productName LIMIT 1")
    Cart_Model getCartItemByName(String productName);

    @Query("SELECT * FROM cart_items")
    List<Cart_Model> getCartItemsOnce();
}