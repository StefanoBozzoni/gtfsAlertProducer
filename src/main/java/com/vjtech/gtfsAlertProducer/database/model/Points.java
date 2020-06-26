package com.vjtech.gtfsAlertProducer.database.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Points {
	private BigDecimal longitude;
	private BigDecimal latitude;
	
	/*
	public Points(BigDecimal lon, BigDecimal lat) {
		// TODO Auto-generated constructor stub
		this.longitude = lon;
		this.latitude = lat;
	}
	*/

	public Points(BigDecimal lon, BigDecimal lat) {
		this.longitude = lon;
		this.latitude = lat;// TODO Auto-generated constructor stub
	}
}