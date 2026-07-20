package com.example.quan_ly_du_an.api;

import com.example.quan_ly_du_an.model.Task;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TaskApi {

    @GET("tasks")
    Call<List<Task>> getTasks();

    @GET("tasks/{id}")
    Call<Task> getTask(@Path("id") String id);

    @POST("tasks")
    Call<Task> createTask(@Body Task task);

    @PUT("tasks/{id}")
    Call<Task> updateTask(
            @Path("id") String id,
            @Body Task task
    );

    @DELETE("tasks/{id}")
    Call<Void> deleteTask(
            @Path("id") String id
    );

}