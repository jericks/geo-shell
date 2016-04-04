.. image:: https://travis-ci.org/jericks/geo-shell.svg?branch=master
    :target: https://travis-ci.org/jericks/geo-shell

geo-shell
---------
geo-shell is an interactive shell for geospatial analysis. 

geo-shell support vector, raster, and tile datasets and includes a map module.

Behind the scenes it uses GeoScript Groovy, GeoTools, and JTS.

Example
-------
$ geo-shell

geo-shell>workspace open --name shps --params /Users/you/Projects/NaturalEarth/LargeScale/10m_cultural

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
workspace open --name shps --params /Users/you/shps

workspce close --name shps

workspace list --name shps

workspace layers --name shps

layer 
-----
layer open --workspace shps --layer states --name us_states

layer close --name us_states

layer list

layer schema --name us_states

layer features --name us_states --filter "NAME='Washington'"

layer count --name us_states

layer remove --workspace shps --layer states

layer create --workspace shps --name points --fields "the_geom=Point EPSG:4326|id=Int|name=String"

layer add --name points --values "the_geom=POINT (1 1)|id=1|name=Seattle"

layer delete --name states --filter "area > 500"

layer copy --input-name states --output-workspace layers.gpkg --output-name countries

layer style set --name us_states --style states.sld

layer style get --name us_states --style states.sld

layer addfields --input-name points --output-workspace directory --output-name points_xy --fields "xcol=Double,ycol=Double"

layer removefields --input-name points_xy --output-workspace mem --output-name points_no_xy --fields xcoord,ycoord

layer updatefield --name points_fields --field wkt --value 'return f.geom.wkt' --script true

layer addareafield --input-name us_states --output-workspace postgis --output-name states_area --area-fieldname area

layer addidfield --input-name us_states --output-workspace postgis --output-name states_area --id-fieldname id --start-value 1

layer addxyfields --input-name points --output-workspace postgis --output-name points_xy --x-fieldname xcoord --y-fieldname ycoord

layer buffer --input-name us_states --output-workspace postgis --output-name states_buffered --distance 10

layer centroid --input-name us_states --output-workspace postgis --output-name states_centroids

layer interiorpoint --input-name us_states --output-workspace postgis --output-name states_interiorpoints

layer random --output-workspace postgis --output-name points --number 100 --geometry 0,0,45,45

layer grid rowcol --output-workspace layers --output-name grid --rows 10 --columns 10 --geometry -180,-90,180,90

layer grid widthheight --output-workspace layers --output-name grid --width 20 --height 15 --geometry -180,-90,180,90

layer extent --input-name polygons --output-workspace memory --output-name extent

layer extents --input-name polygons --output-workspace memory --output-name extents

layer convexhull --input-name polygons --output-workspace memory --output-name convexhull

layer convexhulls --input-name polygons --output-workspace memory --output-name convexhulls

layer delaunay --input-name points --output-workspace mem --output-name delaunay

layer voronoi --input-name points --output-workspace mem --output-name voronoi

layer mincircle --input-name polygons --output-workspace memory --output-name mincircle

layer mincircles --input-name polygons --output-workspace memory --output-name mincircles

layer minrect --input-name polygons --output-workspace memory --output-name minrect

layer minrects --input-name polygons --output-workspace memory --output-name minrects

layer octagonalenvelope --input-name polygons --output-workspace memory --output-name octagonalenvelope

layer octagonalenvelopes --input-name polygons --output-workspace memory --output-name octagonalenvelopes

layer simplify --input-name polys --output-workspace directory --output-name simplified --algorithm tp --distance 120

layer densify --input-name polys --output-workspace postgis --output-name polys_densified --distance 10

layer transform --input-name points --output-workspace mem --output-name polys --transforms "the_geom=buffer(the_geom, 2)|id=id*10"

tile
----
tile open --name states --params states.mbtiles

tile close --name states

tile list

tile info --name states

tile generate --name states --map states_map --start 0 --end 4

tile stitch raster --name osm --format osm_bounds --raster osm_bounds --bounds "-102.360992,47.126213,-100.390320,47.819610,EPSG:4326"

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

map display --name state_map --bounds 0,0,100,100

License
-------
geo-shell is open source and licensed under the MIT License.
