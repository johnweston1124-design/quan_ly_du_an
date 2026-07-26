package com.example.quan_ly_du_an.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Emulator Android Studio
    private static final String BASE_URL = "http://10.0.2.2:3000/api/";

    // Nếu dùng điện thoại thật thì đổi thành
    // http://192.168.x.x:3000/api/

    private static Retrofit retrofit;

    public static Retrofit getClient() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }

        return retrofit;

    }

}
