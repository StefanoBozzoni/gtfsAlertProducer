package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="routes")
@Data
public class Routes implements Serializable {
	@Id
	@Column(name="route_id")
	@Basic(optional=false)
	private Integer routeId;
	@Column(name="agency_id")
	@Basic(optional=false)
	private String agencyId;
	@Column(name="route_short_name")
	@Basic(optional=false)
	private String routeShortName;
	@Column(name="route_long_name")
	@Basic(optional=true)
	private String routeLongName;
	@Column(name="route_type")
	@Basic(optional=false)
	private Integer routeType;
	@Column(name="route_url")
	@Basic(optional=true)
	private String routeUrl;
	@Column(name="route_color")
	@Basic(optional=true)
	private Integer routeColor;
	@Column(name="route_text_color")
	@Basic(optional=true)
	private String routeTextColor;
	private final static long serialVersionUID = -3963590334207829273L;

}
