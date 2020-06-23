package com.vjtech.gtfsAlertProducer.services.session;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Service
public class SessionService {

	//private static final String API_BASE_URL="https://coll-extapi.whereapp.it/";
	
	/*
	@Value("${app.message_api_url}")    
    public void setBaseURL(String mValue) {
    this.API_BASE_URL = mValue;}
    */

	private ISessionService service;

	public SessionService(@Value("${app.message_api_url}") String API_BASE_URL) {

		OkHttpClient okHttpClient = new OkHttpClient.Builder()
				.addInterceptor(new BasicAuthInterceptor("extapi-client", "extapi-v1-secret"))
				.build();

		Retrofit retrofit = new Retrofit.Builder().baseUrl(API_BASE_URL).client(okHttpClient)
				.addConverterFactory(GsonConverterFactory.create()).build();

		service = retrofit.create(ISessionService.class);
	}

	public AccessTokenResponse getAccessToken() throws IOException {
		Call<AccessTokenResponse> retrofitCall = service.getAccessToken("romamobilita@whereapp.it", "prova","password");

		Response<AccessTokenResponse> response = retrofitCall.execute();

		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}

		return response.body();
	}

}