package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class AlertRouteHistoryPK implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2784628969441696637L;
	
	@Basic(optional = false)
    @Column(name = "idalert")
    private int idalert;
    @Basic(optional = false)
    @Column(name = "idroute")
    private int idroute;

    public AlertRouteHistoryPK() {
    }

    public AlertRouteHistoryPK(int idalert, int idroute) {
        this.idalert = idalert;
        this.idroute = idroute;
    }

    /*
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idalert;
        hash += (int) idroute;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AlertRouteHistoryPK)) {
            return false;
        }
        AlertRouteHistoryPK other = (AlertRouteHistoryPK) object;
        if (this.idalert != other.idalert) {
            return false;
        }
        if (this.idroute != other.idroute) {
            return false;
        }
        return true;
    }
    */

}
