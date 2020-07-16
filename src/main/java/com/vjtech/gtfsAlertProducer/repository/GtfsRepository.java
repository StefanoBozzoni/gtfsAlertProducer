package com.vjtech.gtfsAlertProducer.repository;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.vjtech.gtfsAlertProducer.Utils.Constants;
import com.vjtech.gtfsAlertProducer.database.model.AlertHistory;
import com.vjtech.gtfsAlertProducer.database.model.ZetaRoute;

@Component
public class GtfsRepository {

	@Autowired
	ZetaRouteRepository zetaRouteRepository;

	@Autowired
	AlertHistoryRepository alertHistoryRepository;

	private static final Logger log = LoggerFactory.getLogger(GtfsRepository.class);

	public void insertNewArea(Integer idRoute, Geometry geometry, String routeShortDescr, Integer areaId,
			Integer currIdAlert) throws Exception {
		ZetaRoute zrRec = new ZetaRoute();
		zrRec.setGeogArea(geometry);
		zrRec.setIdroute(idRoute);
		zrRec.setText("Linea: " + routeShortDescr);
		zrRec.setIdalert_last(currIdAlert);
		zrRec.setIdarea(areaId);
		try {
			zetaRouteRepository.saveAndFlush(zrRec);
		} catch (Exception e) {
			log.info("Errore salvataggio area in zeta_route");
			throw e;
		}
		log.info("salvato su db");
	}

	public void updateArea(ZetaRoute zr, Geometry geometry, String routeShortDescr, Integer areaId, Integer currIdAlert) throws Exception {
		ZetaRoute zrRec = zr;
		if (zrRec != null) {
			zrRec.setGeogArea(geometry);
			zrRec.setText("Linea: " + routeShortDescr);
			zrRec.setIdarea(areaId);
			zrRec.setIdalert_last(currIdAlert);
			try {
				zetaRouteRepository.saveAndFlush(zrRec);
				log.info("aggiornamento area in tabella route_area");
			} catch (Exception e) {
				log.info("Errore aggiornamento area in tabella route_area");
				throw e;
			}
			
		}
	}

	public ZetaRoute findZetaRouteByIdroute(Integer idRoute) {
		return zetaRouteRepository.findByIdroute(idRoute);
	}

	public void registerAlert(Integer idAlert,String status) {
		AlertHistory alertEntity = new AlertHistory();
		alertEntity.setIdalert(idAlert);
		alertEntity.setStatus(status);
		alertHistoryRepository.save(alertEntity);
	}
	
	public Boolean isAlertElaborated(Integer idAlert) {
		AlertHistory alertEntity = alertHistoryRepository.findById(idAlert).orElse(null);
		
		if (alertEntity!=null) log.info("Alert Entity="+alertEntity.toString());
		
		return (alertEntity!=null && alertEntity.getStatus().equals(Constants.ELABORATA));
	}
	

}
