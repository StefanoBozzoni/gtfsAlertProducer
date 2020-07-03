set search_path to schematest, public;
SET schema 'schematest';
select * from zeta_route zr;
delete from zeta_route ;

insert into zeta_route (idroute,text,geog_area ) values (249,'st_buffer(st_geographyfromtext(''LINESTRING(13 42, 13.1 42.1, 13.2  42.6)''), 100, ''endcap=round join=round'')',
st_buffer(st_geographyfromtext('LINESTRING(13 42, 13.1 42.1, 13.2  42.6)'), 100, 'endcap=round join=round'));

select st_buffer(st_geographyfromtext('LINESTRING(13 42, 13.1 42.1, 13.2  42.6)'), 100, 'endcap=round join=round');
select * from zeta_route zr ;
select * from schematest.trips;

-- per relevare il testo dal buffer:
select st_asText(st_buffer(st_geographyfromtext('LINESTRING(13 42, 13.1 42.1, 13.2  42.6)'), 100, 'endcap=round join=round'));

alter table zeta_route add column idarea int;

CREATE TABLE schematest.trips  (
   route_id  int NOT NULL,
   service_id  int NOT NULL,
   trip_id  varchar(42) NOT NULL,
   trip_headsign  varchar(40) NOT NULL,
   trip_short_name  varchar(8) DEFAULT NULL,
   direction_id  bit(1) DEFAULT NULL,
   block_id  varchar(40) DEFAULT NULL,
   shape_id  varchar(42) NOT NULL,
   wheelchair_accessible  int NOT NULL,
   bikes_allowed  int DEFAULT NULL,
  PRIMARY KEY ( trip_id )
);

CREATE TABLE  shapes  (
   shape_id  varchar(42) NOT NULL,
   shape_pt_lat  decimal(10,7) NOT NULL,
   shape_pt_lon  decimal(10,7) NOT NULL,
   shape_pt_sequence  int NOT NULL,
   shape_dist_traveled  decimal(13,8) NOT NULL
);

CREATE TABLE  routes  (
   route_id  int NOT NULL,
   agency_id  varchar(4) NOT NULL,
   route_short_name  varchar(16) NOT NULL,
   route_long_name  varchar(8) DEFAULT NULL,
   route_type  int NOT NULL,
   route_url  varchar(48) DEFAULT NULL,
   route_color  varchar(6) DEFAULT NULL,
   route_text_color  varchar(6) DEFAULT NULL,
  PRIMARY KEY ( route_id )
) ;

CREATE TABLE  agency  (
   agency_id  varchar(4) NOT NULL,
   agency_name  varchar(10) NOT NULL,
   agency_url  varchar(26) NOT NULL,
   agency_timezone  varchar(11) NOT NULL,
   agency_lang  varchar(2) DEFAULT NULL,
   agency_phone  varchar(8) DEFAULT NULL,
  PRIMARY KEY ( agency_id )
);


ALTER TABLE schematest.shapes ADD CONSTRAINT shapes_pk PRIMARY KEY (shape_id,shape_pt_sequence);

