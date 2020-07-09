package com.vjtech.gtfsAlertProducer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vjtech.gtfsAlertProducer.database.model.Routes;
import com.vjtech.gtfsAlertProducer.database.model.Trips;

@Repository
public interface TripsRepository extends JpaRepository<Trips, Integer> {
  
	List<Trips> findAll();
	
}
