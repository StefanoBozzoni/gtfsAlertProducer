package com.vjtech.gtfsAlertProducer.services.session;

import java.io.IOException;
import java.util.Arrays;

import org.geolatte.geom.jts.JTSConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vjtech.gtfsAlertProducer.services.JobZoneResponse;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaResponse;
import com.vjtech.gtfsAlertProducer.services.model.JobZoneRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Service
public class GtfsService {

	private IGtfsService service;

	public GtfsService(AuthorizationTokenInterceptor authIntercept, @Value("${app.message_api_url}")  String API_BASE_URL, Environment environment) {
		
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();  
		logging.setLevel(HttpLoggingInterceptor.Level.BODY);

		OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
		//add log http in dev env
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("dev"))) {
			okHttpClientBuilder.addInterceptor(logging);
		} 
		
		OkHttpClient okHttpClient = okHttpClientBuilder
				.addInterceptor(authIntercept)
				.build();
			
		Gson gson = new GsonBuilder()
				.serializeSpecialFloatingPointValues() 
				.serializeNulls()
				.setLenient()
				.create();
		
		Retrofit retrofit = new Retrofit.Builder().
				baseUrl(API_BASE_URL).client(okHttpClient)
				.addConverterFactory(JacksonConverterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(gson)).build();

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
		
	public CreateAreaResponse createAreaAstext(CreateAreaRequest areaRequest) throws IOException {
		
		Call<CreateAreaResponse> retrofitCall = service.createAreaAsText(areaRequest);		
		Response<CreateAreaResponse> response = retrofitCall.execute();		
		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}		
		return response.body();
		
	}
	
	public PostMessageByAreaResponse postMessageByArea(PostMessageByAreaRequest messageRequest) throws IOException {
		
		Call<PostMessageByAreaResponse> retrofitCall = service.postMessageByArea(messageRequest);		
		
		Response<PostMessageByAreaResponse> response = retrofitCall.execute();		
		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}		
		return response.body();
		
	}
	
}