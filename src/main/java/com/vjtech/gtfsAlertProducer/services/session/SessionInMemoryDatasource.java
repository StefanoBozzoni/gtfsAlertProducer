package com.vjtech.gtfsAlertProducer.services.session;

import org.springframework.stereotype.Component;

@Component
public class SessionInMemoryDatasource {

	String accessToken  = "";
	String refreshToken = "";
	String md5Checksum  = "";

	public String getMd5Checksum() {
		return md5Checksum;
	}

	public void setMd5Checksum(String md5Checksum) {
		this.md5Checksum = md5Checksum;
	}

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
