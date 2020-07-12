package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="trips")
@Data
public class Trips implements Serializable {
	@Column(name="route_id")
	@Basic(optional=false)
	@SerializedName("route_id")
	@Expose
	public Integer routeId;
	
	@Column(name="service_id")
	@Basic(optional=false)
	@SerializedName("service_id")
	@Expose
	public Integer serviceId;
	
	@Id
	@Column(name="trip_id")
	@Basic(optional=false)
	@SerializedName("trip_id")
	@Expose
	public String tripId;
	
	@Column(name="trip_headsign")
	@Basic(optional=false)
	@SerializedName("trip_headsign")
	@Expose
	public String tripHeadsign;
	
	@Column(name="trip_short_name")
	@Basic(optional=false)
	@SerializedName("trip_short_name")
	@Expose	
	public String tripShortName;
	
	@Column(name="direction_id")
	@Basic(optional=false)
	@SerializedName("direction_id")
	@Expose		
	public Integer directionId;
	
	@Column(name="block_id")
	@Basic(optional=false)
	@SerializedName("block_id")
	@Expose		
	public String blockId;
	
	@Column(name="shape_id")
	@Basic(optional=false)
	@SerializedName("shape_id")
	@Expose		
	public String shapeId;
	
	
	@Column(name="wheelchair_accessible")
	@Basic(optional=false)
	@SerializedName("wheelchair_accessible")
	@Expose		
	public Integer wheelchairAccessible;
	
	
	/*
	@Column(name="bikes_allowed")
	@Basic(optional=true)
	@SerializedName("bikes_allowed")
	@Expose		
	public String bikesAllowed;
	*/
	
	/*
	public Integer getBikesAllowed() {
		return bikesAllowed;
	}

	public void setBikesAllowed(Integer bikesAllowed) {
		this.bikesAllowed = bikesAllowed;
	}
	
	public void setBikesAllowed(String bikesAllowed) {
		if (bikesAllowed.equals(""))
			this.bikesAllowed = 0;
		else	
			this.bikesAllowed = Integer.parseInt(bikesAllowed);
	}
	*/

	private final static long serialVersionUID = 494744249277607275L;

}