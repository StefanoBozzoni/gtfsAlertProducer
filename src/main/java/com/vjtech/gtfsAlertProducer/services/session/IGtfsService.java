package com.vjtech.gtfsAlertProducer.services.session;

import com.vjtech.gtfsAlertProducer.services.JobZoneResponse;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaResponse;
import com.vjtech.gtfsAlertProducer.services.model.JobZoneRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IGtfsService {
	
	@Deprecated
	@POST("v1/createJobZone")
	Call<JobZoneResponse> createJobZone(@Body JobZoneRequest zonerequest);
	
	@POST("v4/uop/messages/byarea")
	Call<PostMessageByAreaResponse> postMessageByArea(@Body PostMessageByAreaRequest byAreaRequest);
		
	@POST("v4/aop/areas")
	Call<CreateAreaResponse> createAreaAsText(@Body CreateAreaRequest areaRequest);
	
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
