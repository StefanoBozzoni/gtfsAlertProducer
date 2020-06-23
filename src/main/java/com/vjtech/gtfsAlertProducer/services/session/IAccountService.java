package com.vjtech.gtfsAlertProducer.services.session;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import com.vjtech.gtfsAlertProducer.services.JobZoneResponse;
import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;
import com.vjtech.gtfsAlertProducer.services.model.JobZoneRequest;

public interface IAccountService {
	
	@POST("v1/createJobZone")
	Call<JobZoneResponse> createJobZone(@Body JobZoneRequest zonerequest);

	/*
    @DELETE("repos/{owner}/{repo}")
    Call<DeletePayload> deleteRepo(@Header("Authorization") String accessToken, @Header("Accept") String apiVersionSpec,
                                   @Path("repo") String repo, @Path("owner") String owner);
    */

	/*
    @POST("user/repos")
    Call<Repository> createRepo(@Body Repository repo, @Header("Authorization") String accessToken,
                                      @Header("Accept") String apiVersionSpec,
                                      @Header("Content-Type") String contentType);
    */

}
