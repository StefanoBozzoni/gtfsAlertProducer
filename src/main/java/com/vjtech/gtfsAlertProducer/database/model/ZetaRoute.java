package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import lombok.Data;

@Entity
@Table(name = "route_area")
//@Data
public class ZetaRoute implements Serializable {

	static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
   
	@Column(name = "idroute")
    private Integer idroute;
    
	@Column(name = "idarea")
	private Integer idarea;

	@Column(name = "idalert_last")
	private Integer idalert_last;

	@Column(name = "geom_route")
	private Geometry geomRoute;

	@Column(name = "geom_area")
	private Geometry geomArea;

	@Column(name = "geog_route")
	private Geometry geogRoute;
	
	@Column(name = "geog_area")
	private Geometry geogArea;

	@Column(name = "text")
	private String text;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdroute() {
		return idroute;
	}

	public void setIdroute(Integer idroute) {
		this.idroute = idroute;
	}

	public Integer getIdarea() {
		return idarea;
	}

	public void setIdarea(Integer idarea) {
		this.idarea = idarea;
	}

	public Geometry getGeomRoute() {
		return geomRoute;
	}

	public void setGeomRoute(Geometry geomRoute) {
		this.geomRoute = geomRoute;
	}

	public Geometry getGeomArea() {
		return geomArea;
	}

	public void setGeomArea(Geometry geomArea) {
		this.geomArea = geomArea;
	}

	public Geometry getGeogRoute() {
		return geogRoute;
	}

	public void setGeogRoute(Geometry geogRoute) {
		this.geogRoute = geogRoute;
	}

	public Geometry getGeogArea() {
		return geogArea;
	}

	public void setGeogArea(Geometry geogArea) {
		this.geogArea = geogArea;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public Integer getIdalert_last() {
		return idalert_last;
	}

	public void setIdalert_last(Integer idalert_last) {
		this.idalert_last = idalert_last;
	}	
	
}
