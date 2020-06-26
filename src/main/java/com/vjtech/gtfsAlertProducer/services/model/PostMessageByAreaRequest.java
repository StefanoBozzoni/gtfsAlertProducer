package com.vjtech.gtfsAlertProducer.services.model;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class PostMessageByAreaRequest implements Serializable {
	private final static long serialVersionUID = 1L;
	
	@SerializedName("severity")
	@Expose
	private Object severity;
	@SerializedName("startDate")
	@Expose
	private String startDate;
	@SerializedName("endDate")
	@Expose
	private String endDate;
	@SerializedName("senderId")
	@Expose
	private Integer senderId;
	@SerializedName("language")
	@Expose
	private String language;
	@SerializedName("categoryCode")
	@Expose
	private String categoryCode;
	@SerializedName("subject")
	@Expose
	private String subject;
	@SerializedName("body")
	@Expose
	private String body;
	@SerializedName("multiArea")
	@Expose
	private Boolean multiArea;
	@SerializedName("areaIds")
	@Expose
	private List<Integer> areaIds = null;
	

}
