package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.locationtech.jts.geom.Geometry;

import lombok.Data;

@Entity
@Table(name = "zeta_route")
@Data
public class ZetaRoute implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "id")
	private Integer id;
	@Column(name = "text")
	private String text;
	@Column(name = "idroute")
	private Integer idroute;
	@Column(name = "idarea")
	private Integer idarea;
	@Lob
	@Column(name = "geom_route")
	private Geometry geomRoute;
	@Lob
	@Column(name = "geom_area")
	private Geometry geomArea;
	@Lob
	@Column(name = "geog_route")
	private Geometry geogRoute;
	@Lob
	@Column(name = "geog_area")
	private Geometry geogArea;
}
