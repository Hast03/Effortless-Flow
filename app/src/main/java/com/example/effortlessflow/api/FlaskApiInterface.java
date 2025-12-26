package com.example.effortlessflow.api;

import com.example.effortlessflow.model.FlaskRequest;
import com.example.effortlessflow.model.FlaskResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface FlaskApiInterface {

    @GET("/")
    Call<FlaskResponse> checkStatus();

    @POST("/generate-subtasks")
    Call<FlaskResponse> generateSubtasks(@Body FlaskRequest request);
}