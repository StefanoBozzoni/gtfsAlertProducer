package com.vjtech.gtfsAlertProducer.services.model;

import java.io.Serializable;
import org.locationtech.jts.geom.Geometry;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreateAreaRequest implements Serializable {
	private final static long serialVersionUID = -6458039420379845811L;
	@SerializedName("senderId")
	@Expose
	public Integer senderId;
	@SerializedName("areaId")
	@Expose
	public Integer areaId;
	@SerializedName("areaName")
	@Expose
	public String areaName;
	@SerializedName("geoArea")
	@Expose
	public Geometry geoArea;
	
	@SerializedName("textArea")
	@Expose
	public String textArea;
	
}