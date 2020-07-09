package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Entity
@Table(name="routes")
@Data
public class Routes implements Serializable {
	@Id
	@Column(name="route_id")
	@Basic(optional=false)
	@SerializedName("route_id")
	@Expose
	private Integer routeId;
	
	@Column(name="agency_id")
	@Basic(optional=false)
	@SerializedName("agency_id")
	@Expose
	private String agencyId;
	
	@Column(name="route_short_name")
	@Basic(optional=false)
	@SerializedName("route_short_name")
	@Expose
	private String routeShortName;
	
	
	@Column(name="route_long_name")	
	@Basic(optional=true)
	@SerializedName("route_long_name")
	@Expose
	private String routeLongName;
	
	@Column(name="route_type")
	@Basic(optional=false)
	@SerializedName("route_type")
	@Expose
	private Integer routeType;
	
	
	@Column(name="route_url")
	@Basic(optional=true)
	@SerializedName("route_url")
	@Expose
	private String routeUrl;
	
	
	@Column(name="route_color")
	@Basic(optional=true)
	@SerializedName("route_color")
	@Expose
	private String routeColor;
	
	
	@Column(name="route_text_color")
	@Basic(optional=true)
	@SerializedName("route_text_color")
	@Expose
	private String routeTextColor;
	
	
	private final static long serialVersionUID = -3963590334207829273L;

}
