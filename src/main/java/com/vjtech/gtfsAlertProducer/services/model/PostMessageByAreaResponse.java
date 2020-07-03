package com.vjtech.gtfsAlertProducer.services.model;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostMessageByAreaResponse implements Serializable {
	private final static long serialVersionUID = 2747064170438502628L;

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

}