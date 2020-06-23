package com.vjtech.gtfsAlertProducer.services;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JobZoneResponse implements Serializable {
	@SerializedName("timestamp")
	@Expose
	public String timestamp;
	@SerializedName("status")
	@Expose
	public String status;
	@SerializedName("errorCode")
	@Expose
	public String errorCode;
	@SerializedName("outcome")
	@Expose
	public String outcome;
	@SerializedName("message")
	@Expose
	public String message;
	private final static long serialVersionUID = -3539306174296659057L;

}