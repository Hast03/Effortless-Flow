//package com.example.effortlessflow.api;
//
//import com.example.effortlessflow.model.GeminiRequest;
//import com.example.effortlessflow.model.GeminiResponse;
//import retrofit2.Call;
//import retrofit2.http.Body;
//import retrofit2.http.POST;
//import retrofit2.http.Query;
//
//public interface GeminiApiInterface {
//    @POST("v1beta/models/gemini-pro:generateContent")
//    Call<GeminiResponse> generateContent(
//            @Query("key") String apiKey,
//            @Body GeminiRequest request
//    );
//}