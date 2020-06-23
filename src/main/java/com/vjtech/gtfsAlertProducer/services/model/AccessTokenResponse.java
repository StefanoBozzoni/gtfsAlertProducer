package com.vjtech.gtfsAlertProducer.services.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccessTokenResponse {

	@SerializedName("access_token")
	@Expose
	public String accessToken;
	@SerializedName("token_type")
	@Expose
	public String tokenType;
	@SerializedName("refresh_token")
	@Expose
	public String refreshToken;
	@SerializedName("expires_in")
	@Expose
	public Integer expiresIn;
	@SerializedName("scope")
	@Expose
	public String scope;

}
