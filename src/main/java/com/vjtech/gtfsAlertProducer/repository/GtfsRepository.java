package com.vjtech.gtfsAlertProducer.repository;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vjtech.gtfsAlertProducer.GtfsAlertProducerApplication;
import com.vjtech.gtfsAlertProducer.database.model.ZetaRoute;

@Component
public class GtfsRepository {

	@Autowired
	ZetaRouteRepository zetaRouteRepository;

	private static final Logger log = LoggerFactory.getLogger(GtfsRepository.class);

	public void insertNewArea(Integer idRoute, Geometry geometry, String routeShortDescr, Integer areaId) {
		ZetaRoute zrRec = new ZetaRoute();
		zrRec.setGeogArea(geometry);
		zrRec.setIdroute(idRoute);
		zrRec.setText("Linea: " + routeShortDescr);
		zrRec.setIdarea(areaId);
		try {
			zetaRouteRepository.saveAndFlush(zrRec);
		} catch (Exception e) {
			log.info("Errore salvataggio area in zeta_route");
		}
		log.info("salvato su db");
	}

	public void updateArea(ZetaRoute zr, Geometry geometry, String routeShortDescr, Integer areaId) {
		ZetaRoute zrRec = zr;
		if (zrRec != null) {
			zrRec.setGeogArea(geometry);
			zrRec.setText("Linea: " + routeShortDescr);
			zrRec.setIdarea(areaId);
			try {
				zetaRouteRepository.saveAndFlush(zrRec);
			} catch (Exception e) {
				log.info("Errore aggiornamento area in zeta_route");
			}
			log.info("salvato su db");
		}
	}

	public ZetaRoute findZetaRouteByIdroute(Integer idRoute) {
		return zetaRouteRepository.findByIdroute(idRoute);
	}

}
