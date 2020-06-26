SET schema 'schematest';
select distinct s.shape_pt_lat as "lat",s.shape_pt_lon as "long", t.shape_id as "name" , '#FFFF00' as "color" , '' as "note" 
from trips t join shapes s on t.shape_id=s.shape_id where t.route_id=249;