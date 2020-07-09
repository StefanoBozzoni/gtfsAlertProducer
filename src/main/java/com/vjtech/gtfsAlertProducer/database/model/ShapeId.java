package com.vjtech.gtfsAlertProducer.database.model;

import java.io.Serializable;

public class ShapeId implements Serializable {

	private static final long serialVersionUID = -4255659277148058884L;
	
	private String shapeId;
	private Integer shapePtSequence;
	
	public ShapeId() {
		this.shapeId = null;
		this.shapePtSequence = null;
	}
	
	public ShapeId(String str, Integer shapePtSequence) {
		this.shapeId = str;
		this.shapePtSequence = shapePtSequence;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((shapeId == null) ? 0 : shapeId.hashCode());
		result = prime * result + ((shapePtSequence == null) ? 0 : shapePtSequence.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShapeId other = (ShapeId) obj;
		if (shapeId == null) {
			if (other.shapeId != null)
				return false;
		} else if (!shapeId.equals(other.shapeId))
			return false;
		if (shapePtSequence == null) {
			if (other.shapePtSequence != null)
				return false;
		} else if (!shapePtSequence.equals(other.shapePtSequence))
			return false;
		return true;
	}



}
