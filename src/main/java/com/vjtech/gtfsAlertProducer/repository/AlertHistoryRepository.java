package com.vjtech.gtfsAlertProducer.repository;

import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vjtech.gtfsAlertProducer.database.model.Agency;
import com.vjtech.gtfsAlertProducer.database.model.AlertHistory;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Integer> {
   
   List<AlertHistory> findAll();   
 	
}
