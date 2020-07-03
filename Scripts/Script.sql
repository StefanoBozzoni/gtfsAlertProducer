SET schema 'schematest';
select distinct s.shape_pt_lat as "lat",s.shape_pt_lon as "long", t.shape_id as "name" , '#FFFF00' as "color" , '' as "note" 
from trips t join shapes s on t.shape_id=s.shape_id where t.route_id=249;

insert into zeta_route (idroute,text, geog_area, idarea) values(249, 'prova', null,0);

select * from zeta_route zr ;