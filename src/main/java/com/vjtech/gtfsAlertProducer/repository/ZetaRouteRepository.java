package com.vjtech.gtfsAlertProducer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vjtech.gtfsAlertProducer.database.model.ZetaRoute;

@Repository
public interface ZetaRouteRepository extends JpaRepository<ZetaRoute, Integer> {

	List<ZetaRoute> findAll();
	ZetaRoute findByIdroute(Integer idRoute);
	List<ZetaRoute> findByIdarea(Integer idArea);

	@Query(value = "select distinct s.shape_pt_lat as lat,s.shape_pt_lon as long, t.shape_id as name , '#FFFF00' as color , 'x' as note from {h-schema}trips t join {h-schema}shapes s on t.shape_id=s.shape_id where t.route_id=:route_id and t.direction_id = '0' ", nativeQuery = true)
	public List<Object[]> findPointsByRouteId(@Param("route_id") int route_id);
	
	/*
    @Modifying
    @Transactional
    @Query(value ="insert into {h-schema}zeta_route (idroute, text, geog_area) values(:idroute, :text, :geog_area)", nativeQuery = true)
    public void insertRoute(@Param("idroute") int idroute, @Param("text") String text, @Param("geog_area") Object geog_area);
	
    @Modifying
    @Transactional
    @Query(value = "update {h-schema}zeta_route z set idarea =:idarea where z.id=:id", nativeQuery = true)
    public void updateRouteWithArea(@Param("id") int id, @Param("idarea") int idarea);
    */
    
}
