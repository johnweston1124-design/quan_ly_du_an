package com.example.quan_ly_du_an.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.quan_ly_du_an.model.History;
import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(History history);

    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<History>> getAllHistoryForUser(int userId);
}
