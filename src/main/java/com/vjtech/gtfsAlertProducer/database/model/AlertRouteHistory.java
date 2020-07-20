package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name="alert_route_history")
@Data
public class AlertRouteHistory implements Serializable {
	private static final long serialVersionUID = 5092647389799705415L;
	
    @EmbeddedId
    protected AlertRouteHistoryPK alertRouteHistoryPK;
    
	@Column(name="status")
	@Basic(optional=false)
	private String status;
}
