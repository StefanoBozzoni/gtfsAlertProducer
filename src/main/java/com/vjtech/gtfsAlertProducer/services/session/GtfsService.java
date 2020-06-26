package com.vjtech.gtfsAlertProducer.services.session;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vjtech.gtfsAlertProducer.services.JobZoneResponse;
import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;
import com.vjtech.gtfsAlertProducer.services.model.JobZoneRequest;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Service
public class GtfsService {

	private IGtfsService service;

	public GtfsService(AuthorizationTokenInterceptor authIntercept, @Value("${app.message_api_url}")  String API_BASE_URL) {

		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.addInterceptor(authIntercept)
				.build();

		Retrofit retrofit = new Retrofit.Builder().baseUrl(API_BASE_URL).client(okHttpClient)
				.addConverterFactory(GsonConverterFactory.create()).build();

		service = retrofit.create(IGtfsService.class);
	}
	
	public JobZoneResponse createJobZone(JobZoneRequest zonerequest) throws IOException {
		
		Call<JobZoneResponse> retrofitCall = service.createJobZone(zonerequest);

		Response<JobZoneResponse> response = retrofitCall.execute();

		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}

		return response.body();
		
	}

}