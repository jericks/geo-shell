package org.geoshell.map

import geoscript.geom.Bounds
import geoscript.proj.Projection
import org.geoshell.Catalog
import org.geoshell.raster.RasterName
import org.geoshell.tile.TileName
import org.geoshell.vector.LayerName
import org.geoshell.map.Map as GeoShellMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.shell.support.util.OsUtils
import org.springframework.stereotype.Component

@Component
class MapCommands implements CommandMarker {

    @Autowired
    Catalog catalog

    @CliCommand(value = "map open", help = "Open a new Map.")
    String open(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name
    ) throws Exception {
        catalog.maps[name] = new org.geoshell.map.Map(name, catalog)
        "Map ${name} opened!"
    }

    @CliCommand(value = "map close", help = "Close a Tile Layer.")
    String close(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name
    ) throws Exception {
        org.geoshell.map.Map map = catalog.maps[name]
        if (map) {
            catalog.maps.remove(name)
            "Map ${name} closed!"
        } else {
            "Unable to find Map ${name}"
        }
    }

    @CliCommand(value = "map list", help = "List open Maps.")
    String list() throws Exception {
        catalog.maps.collect{MapName name, GeoShellMap map ->
            "${name}"
        }.join(OsUtils.LINE_SEPARATOR)
    }

    @CliCommand(value = "map layers", help = "List the Map's Layers.")
    String info(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name
    ) throws Exception {
        org.geoshell.map.Map map = catalog.maps[name]
        if (map) {
            String str = ""
            map.mapLayerNames.each { MapLayerName layerName ->
                str += "${layerName}" + OsUtils.LINE_SEPARATOR
            }
            str
        } else {
            "Unable to find Map ${name}"
        }
    }

    @CliCommand(value = "map add layer", help = "Add a Layer.")
    String addLayer(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name,
            @CliOption(key = "layer", mandatory = true, help = "The layer") LayerName layerName,
            @CliOption(key = "mapLayerName", mandatory = false, help = "The map layer name") String mapLayerName
    ) throws Exception {
        org.geoshell.map.Map map = catalog.maps[name]
        if (map) {
            MapLayerName mln = new MapLayerName(mapLayerName ?: layerName.name)
            map.addLayer(mln, layerName)
            "Added ${mln} layer to map ${name}"
        } else {
            "Unable to find Map ${name}"
        }
    }

    @CliCommand(value = "map add raster", help = "Add a Raster.")
    String addRaster(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name,
            @CliOption(key = "raster", mandatory = true, help = "The raster") RasterName rasterName,
            @CliOption(key = "mapLayerName", mandatory = false, help = "The map layer name") String mapLayerName
    ) throws Exception {
        org.geoshell.map.Map map = catalog.maps[name]
        if (map) {
            MapLayerName mln = new MapLayerName(mapLayerName ?: rasterName.name)
            map.addLayer(mln, rasterName)
            "Added ${mln} layer to map ${name}"
        } else {
            "Unable to find Map ${name}"
        }
    }

    @CliCommand(value = "map add tile", help = "Add a Tile.")
    String addTile(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name,
            @CliOption(key = "tile", mandatory = true, help = "The tile") TileName tileName,
            @CliOption(key = "mapLayerName", mandatory = false, help = "The map layer name") String mapLayerName
    ) throws Exception {
        org.geoshell.map.Map map = catalog.maps[name]
        if (map) {
            MapLayerName mln = new MapLayerName(mapLayerName ?: tileName.name)
            map.addLayer(mln, tileName)
            "Added ${mln} layer to map ${name}"
        } else {
            "Unable to find Map ${name}"
        }
    }

    @CliCommand(value = "map remove layer", help = "Remove a Layer.")
    String removeLayer(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name,
            @CliOption(key = "layer", mandatory = true, help = "The layer name") MapLayerName layerName
    ) throws Exception {
        org.geoshell.map.Map map = catalog.maps[name]
        if (map) {
            catalog.maps[name].removeLayer(layerName)
            "Removed ${layerName} layer from map ${name}"
        } else {
            "Unable to find Map ${name}"
        }
    }

    @CliCommand(value = "map reorder", help = "Reorder a Layer in the Map.")
    String reorder(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name,
            @CliOption(key = "layer", mandatory = true, help = "The layer name") MapLayerName layerName,
            @CliOption(key = "order", mandatory = true, help = "The order parameters") String order
    ) throws Exception {
        org.geoshell.map.Map map = catalog.maps[name]
        if (map) {
            map.moveMapLayer(layerName, order)
        } else {
            "Unable to find Map ${name}"
        }

    }

    @CliCommand(value = "map draw", help = "Draw a map.")
    String render(
            @CliOption(key = "name", mandatory = true, help = "The map name") MapName name,
            @CliOption(key = "bounds", mandatory = false, help = "The Bounds") String bounds,
            @CliOption(key = "projection", mandatory = false, help = "The Projection") String projection,
            @CliOption(key = "width", mandatory = false, unspecifiedDefaultValue = "600", specifiedDefaultValue = "600", help = "The width") int width,
            @CliOption(key = "height", mandatory = false, unspecifiedDefaultValue = "400", specifiedDefaultValue = "400", help = "The height") int height,
            @CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "png", specifiedDefaultValue = "png", help = "The type") String type,
            @CliOption(key = "file", mandatory = false, help = "The file") File file,
            @CliOption(key = "background-color", mandatory = false, help = "The background color") String backgroundColor
    ) throws Exception {
        org.geoshell.map.Map map = catalog.maps[name]
        if (map) {
            geoscript.render.Map mapRenderer = map.getMap(
                    bounds: bounds ? Bounds.fromString(bounds) : null,
                    projection: projection ? new Projection(projection) : null,
                    width: width,
                    height: height,
                    type: type,
                    backgroundColor: backgroundColor
            )
            file = file ? file : new File("${['pdf', 'svg'].contains(type) ? "document" : "image"}.${type}")
            mapRenderer.render(file)
            "Done drawing ${file.absolutePath}!"
        } else {
            "Unable to find Map ${name}"
        }
    }
}
