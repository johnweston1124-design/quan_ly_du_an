package com.example.quan_ly_du_an.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import java.util.List;

@Dao
public interface SearchDao {
    @Query("SELECT * FROM comments WHERE content LIKE '%' || :query || '%'")
    LiveData<List<Comment>> searchComments(String query);
}
