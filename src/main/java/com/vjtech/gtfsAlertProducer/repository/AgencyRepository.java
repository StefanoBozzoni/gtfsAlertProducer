package com.vjtech.gtfsAlertProducer.repository;

import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vjtech.gtfsAlertProducer.database.model.Agency;

@Repository
public interface AgencyRepository extends JpaRepository<Agency,Long> {
   List<Agency> findAll();
   
   Agency findById(long id);
   
   /*
   @Query(value = "Select Rule_Id, Rule_Sql,S_System,S_Entity, s_table_name,Rule_Sql_Type "
	        + "FROM rule_master TRM ,T_SYSTEM_CONFIG TSC  Where"
	        + " Tsc.Parameter_Name='APPLICABLE_RULE_TYPE' "
	        + " And Tsc.Parameter_KEY= :systemType"
	        + " AND Tsc.Parameter_Value=trm.rule_type  "
	        + " And Rule_Status='ON'"
	        + " Order By Rule_Priority", nativeQuery = true)
   */
   
    @Query(value = "select distinct s.shape_pt_lat as 'lat',s.shape_pt_lon as 'long', t.shape_id as 'name' , '#FFFF00' as color , '' as note "+
    			   "from trips t join shapes s on t.shape_id=s.shape_id where t.route_id=:route_id", nativeQuery = true)   
	public List<Object[]> findPointsByRouteId(@Param("route_id") int route_id);
	
}
