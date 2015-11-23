package org.geoshell.tile

import geoscript.geom.Bounds
import geoscript.layer.TileGenerator
import geoscript.layer.TileLayer
import geoscript.layer.TileRenderer
import org.geoshell.Catalog
import org.geoshell.map.MapName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.shell.support.util.OsUtils
import org.springframework.stereotype.Component

@Component
class TileCommands implements CommandMarker {

    @Autowired
    Catalog catalog

    @CliCommand(value = "tile open", help = "Open a Tile Layer.")
    String open(
            @CliOption(key = "name", mandatory = true, help = "The tile name") TileName name,
            @CliOption(key = "params", mandatory = true, help = "The connection parameters") String params
    ) throws Exception {
        TileLayer tileLayer = TileLayer.getTileLayer(params)
        if (tileLayer) {
            catalog.tiles[name] = tileLayer
            "Tile Layer ${name} opened!"
        } else {
            "Could not create Tile Layer ${name} from ${params}!"
        }
    }

    @CliCommand(value = "tile close", help = "Close a Tile Layer.")
    String close(
            @CliOption(key = "name", mandatory = true, help = "The tile name") TileName name
    ) throws Exception {
        TileLayer tileLayer = catalog.tiles[name]
        if (tileLayer) {
            tileLayer.close()
            catalog.tiles.remove(name)
            "Tile Layer ${name} closed!"
        } else {
            "Unable to find Tile Layer ${name}"
        }
    }

    @CliCommand(value = "tile list", help = "List open Tile Layers.")
    String list() throws Exception {
        catalog.tiles.collect{TileName name, TileLayer tileLayer ->
            "${name} = ${tileLayer.class.simpleName}"
        }.join(OsUtils.LINE_SEPARATOR)
    }

    @CliCommand(value = "tile info", help = "Get information about a Tile Layer.")
    String info(
            @CliOption(key = "name", mandatory = true, help = "The tile name") TileName name
    ) throws Exception {
        TileLayer tileLayer = catalog.tiles[name]
        if (tileLayer) {
            tileLayer.name + OsUtils.LINE_SEPARATOR +
                    tileLayer.pyramid.csv
        } else {
            "Unable to find Tile Layer ${name}"
        }
    }

    @CliCommand(value = "tile generate", help = "Generate tiles for a Tile Layer.")
    String generate(
            @CliOption(key = "name", mandatory = true, help = "The tile name") TileName name,
            @CliOption(key = "map", mandatory = true, help = "The map name") MapName mapName,
            @CliOption(key = "start", mandatory = true, help = "The map name") int startZoom,
            @CliOption(key = "end", mandatory = true, help = "The map name") int endZoom,
            @CliOption(key = "bounds", mandatory = false, help = "The map name") String bounds,
            @CliOption(key = "missingOnly", specifiedDefaultValue = "false", unspecifiedDefaultValue = "false", mandatory = false, help = "The map name") boolean missingOnly,
            @CliOption(key = "verbose", specifiedDefaultValue = "false", unspecifiedDefaultValue = "false", mandatory = false, help = "The map name") boolean verbose
    ) throws Exception {
        TileLayer tileLayer = catalog.tiles[name]
        if (tileLayer) {
            org.geoshell.map.Map map = catalog.maps[mapName]
           if (map) {
                TileRenderer tileRenderer = TileLayer.getTileRenderer(tileLayer, map.getLayers())
                TileGenerator generator = new TileGenerator(verbose: verbose)
                generator.generate(tileLayer, tileRenderer, startZoom, endZoom,
                        bounds: bounds ? Bounds.fromString(bounds) : null,
                        missingOnly: missingOnly
                )
                "Tiles generated!"
            } else {
                "Unable to find Map ${mapName}"
            }
        } else {
            "Unable to find Tile Layer ${name}"
        }
    }

}
