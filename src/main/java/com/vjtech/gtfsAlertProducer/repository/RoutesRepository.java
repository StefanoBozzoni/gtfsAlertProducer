package com.vjtech.gtfsAlertProducer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vjtech.gtfsAlertProducer.database.model.Routes;

@Repository
public interface RoutesRepository extends JpaRepository<Routes, Integer> {
  
	List<Routes> findAll();
    //Routes findById(Integer id);

    @Query(value = "select distinct s.shape_pt_lat as lat,s.shape_pt_lon as lon, s.shape_id as name ,s.shape_pt_sequence, '#FFFF00' as color , 'x' as note from {h-schema}trips t join {h-schema}shapes s on t.shape_id=s.shape_id where t.route_id=:route_id order by s.shape_id,s.shape_pt_sequence", nativeQuery = true)   
	public List<Object[]> findPointsByRouteId(@Param("route_id") int route_id);
	
    @Query(value = "select distinct s.shape_pt_lat as lat,s.shape_pt_lon as lon, s.shape_id as name ,s.shape_pt_sequence, '#FFFF00' as color , 'x' as note from {h-schema}shapes s where shape_id in (select shape_id from {h-schema}trips where route_id =:route_id and coalesce(direction_id,false) = false) order by s.shape_id, s.shape_pt_sequence", nativeQuery = true)   
	public List<Object[]> findPointsByRouteId2(@Param("route_id") long route_id);
	
}
