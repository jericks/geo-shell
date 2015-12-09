.. image:: https://travis-ci.org/jericks/geo-shell.svg?branch=master
    :target: https://travis-ci.org/jericks/geo-shell

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
layer open --workspace shps --layer states --name us_states

layer close --name us_states

layer list

layer schema --name us_states

layer count --name us_states

layer style set --name us_states --style states.sld

layer style get --name us_states --style states.sld

layer buffer --input-name us_states --output-workspace postgis --output-name states_buffered --distance 10

layer centroid --input-name us_states --output-workspace postgis --output-name states_centroids

layer random --output-workspace postgis --output-name points --number 100 --geometry 0,0,45,45

tile
----
tile open --name states --params states.mbtiles

tile close --name states

tile list

tile info --name states

tile generate --name states --map states_map --start 0 --end 4

format
------
format open --name earth --input earth.tif

format close --name earth

format list

format rasters --name earth

raster
------
raster open --format earth --raster earth

raster close --name earth

raster list

raster info --name earth

raster crop --name earth --output-format cropped_earth --output-name cropped_earth --geometry 0,0,45,45

raster reproject --name earth --output-format earth_reprojected --output-name earth-reprojected --projection EPSG:4326

raster style set --name earth --style earth.sld

raster style get --name earth --style earth.sld

map
---
map open --name state_map

map close --name state_map

map list

map layers --name state_map

map add layer --name state_map --layer states

map add raster --name state_map --raster usa_dem

map add tile --name state_map --tile osm

map remove layer --name state_map --layer states

map reorder --name state_map --layer states --order first | last | up | down | 1 | 2

map draw --name state_map --bounds 0,0,100,100

License
-------
geo-shell is open source and licensed under the MIT License.
