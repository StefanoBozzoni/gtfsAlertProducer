package com.vjtech.gtfsAlertProducer.services.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreateAreaResponse implements Serializable {

	@SerializedName("areaId")
	@JsonProperty("areaId")
	@Expose
	public Integer areaId;
	private final static long serialVersionUID = -8826451513958704660L;

}