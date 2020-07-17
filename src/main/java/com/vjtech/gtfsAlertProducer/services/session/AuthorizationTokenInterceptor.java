package com.vjtech.gtfsAlertProducer.services.session;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;
import lombok.NonNull;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class AuthorizationTokenInterceptor implements Interceptor {

	@Autowired
	ApplicationBean applicationBean;

	@Autowired
	SessionService sessionService;

	@Override
	public Response intercept(Chain chain) throws IOException {
		String accessToken = applicationBean.getAccessToken();
		Request request = newRequestWithAccessToken(chain.request(), accessToken);
		Response response = chain.proceed(request);

		if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
			AccessTokenResponse newAccessTokenResp = sessionService.getAccessToken();
			String newAccessToken = newAccessTokenResp.accessToken;

			if (accessToken != newAccessToken) {
				//we memorize the new tokens in memory
				applicationBean.setAccessToken(newAccessToken);
				applicationBean.setRefreshToken(newAccessTokenResp.refreshToken);
				// if accessToken is changed we need another accessToken
				response.close();
				return chain.proceed(newRequestWithAccessToken(request, newAccessToken));
			} else {
				// if accessToken isn't changed we use the refreshToken
				final String refreshToken = applicationBean.getRefreshToken();
				response.close();
				return chain.proceed(newRequestWithAccessToken(request, refreshToken));
			}
		}
		
		return response;

	}

	@NonNull
	private Request newRequestWithAccessToken(@NonNull Request request, @NonNull String accessToken) {
		return request.newBuilder().header("Authorization", "Bearer " + accessToken).build();
	}

}