//package com.example.orderingsystem;
//
//import com.google.firebase.appdistribution.gradle.ApiService;
//import com.squareup.moshi.Moshi;
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;
//
//import retrofit2.Retrofit;
//import retrofit2.converter.moshi.MoshiConverterFactory;
//
//public class RetrofitInstance {
//    private static final String BASE_URL = "https://localhost:8080/";
//
//    private static final Moshi moshi = new Moshi.Builder()
//            .add(new KotlinJsonAdapterFactory())
//            .build();
//
//    // Volatile variable for thread safety with double-checked locking
//    private static volatile ApiService api;
//
//    // Private constructor to prevent instantiation
//    private RetrofitInstance() {}
//
//    // Public method that replaces "by lazy"
//    public static ApiService getApi() {
//        if (api == null) {
//            synchronized (RetrofitInstance.class) {
//                if (api == null) {
//                    api = new Retrofit.Builder()
//                            .baseUrl(BASE_URL)
//                            .addConverterFactory(MoshiConverterFactory.create(moshi))
//                            .build()
//                            .create(ApiService.class);
//                }
//            }
//        }
//        return api;
//    }
//}
