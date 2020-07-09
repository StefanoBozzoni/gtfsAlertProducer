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
@Table(name="agency")
@Data
public class Agency implements Serializable {
	@Id
	@Column(name="agency_id")
	@Basic(optional=false)
	@SerializedName("agency_id")
	@Expose
	private String agencyId;
	
	@Column(name="agency_name")
	@Basic(optional=false)
	@SerializedName("agency_name")
	@Expose
	private String agencyName;
	
	@Column(name="agency_url")
	@Basic(optional=false)
	@SerializedName("agency_url")
	@Expose
	private String agencyUrl;
	
	@Column(name="agency_timezone")
	@Basic(optional=false)
	@SerializedName("agency_timezone")
	@Expose
	private String agencyTimezone;
	
	@Column(name="agency_lang")
	@Basic(optional=true)
	@SerializedName("agency_lang")
	@Expose
	private String agencyLang;
	
	@Column(name="agency_phone")
	@Basic(optional=true)
	@SerializedName("agency_phone")
	@Expose
	private String agencyPhone;
	private final static long serialVersionUID = 4864197710976395996L;
}