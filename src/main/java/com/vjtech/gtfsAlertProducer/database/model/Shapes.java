package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Entity
@Table(name="shapes")
@IdClass(ShapeId.class)
@Data
public class Shapes implements Serializable {
	@Id
	@Column(name="shape_id")
	@Basic(optional=false)
	@SerializedName("shape_id")
	@Expose
	private String shapeId;
	
	@Column(name="shape_pt_lat")
	@Basic(optional=false)
	@SerializedName("shape_pt_lat")
	@Expose
	private Double shapePtLat;
	
	@Column(name="shape_pt_lon")
	@Basic(optional=false)
	@SerializedName("shape_pt_lon")
	@Expose
	private Double shapePtLon;

	@Id
	@Column(name="shape_pt_sequence")
	@Basic(optional=false)
	@SerializedName("shape_pt_sequence")
	@Expose
	private Integer shapePtSequence;
	
	@Column(name="shape_dist_traveled")
	@Basic(optional=false)
	@SerializedName("shape_dist_traveled")
	@Expose
	public Double shapeDistTraveled;
	private final static long serialVersionUID = -6050245899356087515L;

}