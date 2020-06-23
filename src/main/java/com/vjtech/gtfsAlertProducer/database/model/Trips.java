package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="trips")
@Data
public class Trips implements Serializable {
	@Column(name="route_id")
	@Basic(optional=false)
	public Integer routeId;
	@Column(name="service_id")
	@Basic(optional=false)
	public Integer serviceId;
	@Id
	@Column(name="trip_id")
	@Basic(optional=false)
	public String tripId;
	@Column(name="trip_headsign")
	@Basic(optional=false)
	public String tripHeadsign;
	@Column(name="trip_short_name")
	@Basic(optional=true)
	public String tripShortName;
	@Column(name="direction_id")
	@Basic(optional=true)
	public Integer directionId;
	@Column(name="block_id")
	@Basic(optional=true)
	public String blockId;
	@Column(name="shape_id")
	@Basic(optional=false)
	public String shapeId;
	@Column(name="wheelchair_accessible")
	@Basic(optional=false)
	public Integer wheelchairAccessible;
	@Column(name="bikes_allowed")
	@Basic(optional=true)
	public String bikesAllowed;
	
	private final static long serialVersionUID = 494744249277607275L;

}