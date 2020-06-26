package com.vjtech.gtfsAlertProducer.services.session;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import com.vjtech.gtfsAlertProducer.services.JobZoneResponse;
import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;
import com.vjtech.gtfsAlertProducer.services.model.JobZoneRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaRequest;

public interface IGtfsService {
	
	@POST("v1/createJobZone")
	Call<JobZoneResponse> createJobZone(@Body JobZoneRequest zonerequest);

	
	@POST("v4/uop/messages/byarea")
	Call<JobZoneResponse> postMessageByArea(@Body PostMessageByAreaRequest byAreaRequest);
	

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
