set search_path to schematest, public;
SET schema 'schematest';
select * from zeta_route zr;

delete from zeta_route ;

insert into zeta_route (idroute,text,geog_area ) values (249,'st_buffer(st_geographyfromtext(''LINESTRING(13 42, 13.1 42.1, 13.2  42.6)''), 100, ''endcap=round join=round'')',
st_buffer(st_geographyfromtext('LINESTRING(13 42, 13.1 42.1, 13.2  42.6)'), 100, 'endcap=round join=round'));

select st_buffer(st_geographyfromtext('LINESTRING(13 42, 13.1 42.1, 13.2  42.6)'), 100, 'endcap=round join=round');
select * from zeta_route zr ;

-- per relevare il testo dal buffer:
select st_asText(st_buffer(st_geographyfromtext('LINESTRING(13 42, 13.1 42.1, 13.2  42.6)'), 100, 'endcap=round join=round'));


alter table zeta_route add column idarea int;