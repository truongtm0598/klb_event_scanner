package com.klb.klbeventscanner.network;

import com.klb.klbeventscanner.models.AppRequestBody;
import com.klb.klbeventscanner.models.UserInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @PUT("api/v1/Contact/{id}")
    Call<UserInfo> getInfoUser(
            @Path("id") String id,
            @Body AppRequestBody appRequestBody
    );
}