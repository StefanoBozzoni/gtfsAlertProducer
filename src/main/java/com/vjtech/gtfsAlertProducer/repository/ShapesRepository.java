package com.vjtech.gtfsAlertProducer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vjtech.gtfsAlertProducer.database.model.Routes;
import com.vjtech.gtfsAlertProducer.database.model.ShapeId;
import com.vjtech.gtfsAlertProducer.database.model.Shapes;
import com.vjtech.gtfsAlertProducer.database.model.Trips;

@Repository
public interface ShapesRepository extends JpaRepository<Shapes, ShapeId> {
  
	List<Shapes> findAll();
	
}
