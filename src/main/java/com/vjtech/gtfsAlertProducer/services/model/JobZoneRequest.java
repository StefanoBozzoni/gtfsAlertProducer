package com.vjtech.gtfsAlertProducer.services.model;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JobZoneRequest implements Serializable {
	private final static long serialVersionUID = -2766363765547830645L;
	
	@SerializedName("body")
	@Expose
	public String body;
	@SerializedName("startDate")
	@Expose
	public String startDate;
	@SerializedName("endDate")
	@Expose
	public String endDate;
	@SerializedName("categoryCode")
	@Expose
	public String categoryCode;
	@SerializedName("multiArea")
	@Expose
	public String multiArea;
	@SerializedName("subject")
	@Expose
	public String subject;
	@SerializedName("transitEnabled")
	@Expose
	public Integer transitEnabled;
	@SerializedName("url")
	@Expose
	public String url;
	@SerializedName("areaCode")
	@Expose
	public List<String> areaCode = null;
	@SerializedName("mail")
	@Expose
	public String mail;
	
}