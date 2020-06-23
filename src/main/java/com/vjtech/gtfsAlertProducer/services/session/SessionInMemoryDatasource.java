package com.vjtech.gtfsAlertProducer.services.session;

import org.springframework.stereotype.Component;

@Component
public class SessionInMemoryDatasource {

	String accessToken  = "";
	String refreshToken = "";

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	void clear() {
		accessToken  = "";
		refreshToken = "";
	}

}
