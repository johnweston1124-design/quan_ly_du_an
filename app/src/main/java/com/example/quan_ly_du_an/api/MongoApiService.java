package com.example.quan_ly_du_an.api;

import com.example.quan_ly_du_an.model.User;
import com.example.quan_ly_du_an.model.Task;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MongoApiService {

    @POST("api/users")
    Call<User> registerUser(@Body User user);

    @POST("api/tasks")
    Call<Task> createTask(@Body Task task);
}
