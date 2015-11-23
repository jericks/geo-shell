geo-shell
---------
geo-shell is an interactive shell for geospatial analysis. 

geo-shell support vector, raster, and tile datasets and includes a map module.

Behind the scenes it uses GeoScript Groovy, GeoTools, and JTS.

Example
-------
geo-shell>workspace open --name shps --params /Users/jericks/Projects/NaturalEarth/LargeScale/10m_cultural
Workspace shps opened!

geo-shell>workspace layers --name shps
10m_admin_0_breakaway_disputed_areas
10m_admin_1_states_provinces_lines_shp
10m_us_parks_line
10m_admin_0_scale_ranks_with_minor-islands
10m_admin_0_boundary_lines_maritime_indicator
10m_admin_0_boundary_breakaway_disputed_areas

geo-shell>layer open --workspace shps --layer 10m_roads

workspace
---------
workspace open --name shps --params /Users/joe/shps
workspce close --name shps
workspace list --name shps
workspace layers --name shps

layer 
-----
buffer      
centroid    
close       
count       
draw        
list
open        
random      
schema

tile
----
tile open --name states --params states.mbtiles
tile info --name states
tile generate --name states --map states_map --start 0 --end 4
tile close --name states

map
---
map open --name state_map
map add layer --name state_map --layer states
map add raster --name state_map --raster usa_dem
map add tile --name state_map --tile osm
map remove layer --name state_map --layer states
map reorder --name state_map --layer states --order first | last | up | down | 1 | 2
map render --name state_map --bounds 0,0,100,100
