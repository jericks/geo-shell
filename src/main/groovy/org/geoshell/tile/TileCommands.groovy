package org.geoshell.tile

import geoscript.geom.Bounds
import geoscript.layer.Format
import geoscript.layer.ImageTileLayer
import geoscript.layer.Layer
import geoscript.layer.Pyramid
import geoscript.layer.Raster
import geoscript.layer.Tile
import geoscript.layer.TileCursor
import geoscript.layer.TileGenerator
import geoscript.layer.TileLayer
import geoscript.layer.TileRenderer
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.geoshell.map.MapName
import org.geoshell.raster.FormatName
import org.geoshell.raster.RasterName
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
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

    @CliCommand(value = "tile tiles", help = "List tiles within a given bounds.")
    String tiles(
            @CliOption(key = "name", mandatory = true, help = "The tile name") TileName name,
            @CliOption(key = "bounds", mandatory = true, help = "The bounds") String boundsStr,
            @CliOption(key = "z", mandatory = true, help = "The zoom level") long z
    ) throws Exception {
        TileLayer tileLayer = catalog.tiles[name]
        if (tileLayer) {
            Pyramid pyramid = tileLayer.pyramid
            Bounds bounds = Bounds.fromString(boundsStr)
            if (!bounds.proj) {
                bounds.proj = pyramid.proj
            } else if (bounds.proj != pyramid.bounds) {
                bounds = bounds.reproject(pyramid.proj)
            }
            Map tileCoords = pyramid.getTileCoordinates(bounds, z)
            long minX = tileCoords.minX
            long minY = tileCoords.minY
            long maxX = tileCoords.maxX
            long maxY = tileCoords.maxY
            StringBuilder stringBuilder = new StringBuilder()
            (minY..maxY).each { long y ->
                (minX..maxX).each { long x ->
                    stringBuilder.append("${z}/${x}/${y}" + OsUtils.LINE_SEPARATOR)
                }
            }
            stringBuilder.toString()
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

    @CliCommand(value = "tile delete", help = "Delete tiles from a Tile Layer.")
    String delete(
            @CliOption(key = "name",   mandatory = true, help = "The tile name") TileName name,
            @CliOption(key = "tile",   mandatory = false, help = "The tile z/x/y") String tile,
            @CliOption(key = "bounds", mandatory = false, help = "The bounds") String bounds,
            @CliOption(key = "width",  mandatory = false, unspecifiedDefaultValue = "400", specifiedDefaultValue = "400", help = "The width") int width,
            @CliOption(key = "height", mandatory = false, unspecifiedDefaultValue = "400", specifiedDefaultValue = "400", help = "The height") int height,
            @CliOption(key = "z",      mandatory = false, unspecifiedDefaultValue = "-1", specifiedDefaultValue = "0", help = "The zoom level") long z,
            @CliOption(key = "minx",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The min x or column") long minX,
            @CliOption(key = "miny",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The min y or row") long minY,
            @CliOption(key = "maxx",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The max x or column") long maxX,
            @CliOption(key = "maxy",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The max y or row") long maxY
    )  throws Exception {
        TileLayer tileLayer = catalog.tiles[name]
        if (tileLayer) {
            String result = ""
            if (tile) {
                result = "Deleting tile ${tile}"
                List parts = tile.split("/")
                tileLayer.delete(tileLayer.get(parts[0] as long, parts[1] as long, parts[2] as long))
            } else {
                TileCursor tileCursor
                if (bounds && !z) {
                    result = "Deleting tiles in Bounds ${bounds} with width ${width} and height ${height}"
                    tileCursor = tileLayer.tiles(Bounds.fromString(bounds), width, height)
                } else if (bounds && z) {
                    result = "Deleting tiles in Bounds ${bounds} at z level ${z}"
                    tileCursor = tileLayer.tiles(Bounds.fromString(bounds), z)
                } else if (z && minX > -1 && minY > -1 && maxX > -1 && maxY > -1) {
                    result = "Deleting tiles at z level ${z} between ${minX}, ${minY}, ${maxX}, ${maxY}"
                    tileCursor = tileLayer.tiles(z, minX, minY, maxX, maxY)
                } else if (z) {
                    result = "Deleting tiles at z level ${z}"
                    tileCursor = tileLayer.tiles(z)
                } else {
                    result = "Wrong combination of options to delete tiles!"
                }
                tileLayer.delete(tileCursor)
            }
            result
        } else {
            "Unable to find Tile Layer ${name}"
        }
    }

    @CliCommand(value = "tile stitch raster", help = "Create a Raster from a Tile Layer.")
    String stitchRaster(
            @CliOption(key = "name",   mandatory = true, help = "The tile name") TileName tileName,
            @CliOption(key = "format", mandatory = true, help = "The raster format name") FormatName formatName,
            @CliOption(key = "raster", mandatory = true, help = "The raster name") String rasterName,
            @CliOption(key = "bounds", mandatory = false, help = "The bounds") String bounds,
            @CliOption(key = "width",  mandatory = false, unspecifiedDefaultValue = "400", specifiedDefaultValue = "400", help = "The raster width") int width,
            @CliOption(key = "height", mandatory = false, unspecifiedDefaultValue = "400", specifiedDefaultValue = "400", help = "The raster height") int height,
            @CliOption(key = "z",      mandatory = false, unspecifiedDefaultValue = "-1", specifiedDefaultValue = "0", help = "The zoom level") long z,
            @CliOption(key = "minx",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The min x or column") long minX,
            @CliOption(key = "miny",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The min y or row") long minY,
            @CliOption(key = "maxx",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The max x or column") long maxX,
            @CliOption(key = "maxy",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The max y or row") long maxY
    ) {
        TileLayer tileLayer = catalog.tiles[tileName]
        if (tileLayer) {
            if (!tileLayer instanceof ImageTileLayer) {
                return "Tile Layer must be an Image Tile Layer!"
            }
            Format format = catalog.formats[formatName]
            if (format) {
                ImageTileLayer imageTileLayer = tileLayer as ImageTileLayer
                Raster raster
                if (bounds && z == -1) {
                    Bounds b = Bounds.fromString(bounds)
                    if (b.proj && !b.proj.equals(tileLayer.pyramid.proj)) {
                        b = b.reproject(tileLayer.pyramid.proj)
                    }
                    raster = imageTileLayer.getRaster(b, width, height)
                } else if (bounds && z > -1) {
                    Bounds b = Bounds.fromString(bounds)
                    if (b.proj && !b.proj.equals(tileLayer.pyramid.proj)) {
                        b = b.reproject(tileLayer.pyramid.proj)
                    }
                    raster = imageTileLayer.getRaster(imageTileLayer.tiles(b, z))
                } else if (z > -1 && minX > -1 && minY > -1 && maxX > -1 && maxY > -1) {
                    raster = imageTileLayer.getRaster(imageTileLayer.tiles(z, minX, minY, maxX, maxY))
                } else if (z > -1) {
                    raster = imageTileLayer.getRaster(imageTileLayer.tiles(z))
                } else {
                    return "Wrong combination of options for stitching together a raster from a tile layer!"
                }
                format.write(raster)
                catalog.rasters[new RasterName(rasterName)] = raster
                "Done stitching Raster ${rasterName} from ${tileName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }  
        } else {
            "Unable to find Tile Layer ${tileName}"
        }
    }

    @CliCommand(value = "tile vector grid", help = "Create a Vector Grid Layer from the pyramid of a Tile Layer.")
    String vectorGrid(
            @CliOption(key = "name",   mandatory = true, help = "The tile name") TileName tileName,
            @CliOption(key = "workspace", mandatory = true, help = "The workspace name") WorkspaceName workspaceName,
            @CliOption(key = "layer", mandatory = true, help = "The layer name") String layerName,
            @CliOption(key = "bounds", mandatory = false, help = "The bounds") String bounds,
            @CliOption(key = "width",  mandatory = false, unspecifiedDefaultValue = "400", specifiedDefaultValue = "400", help = "The raster width") int width,
            @CliOption(key = "height", mandatory = false, unspecifiedDefaultValue = "400", specifiedDefaultValue = "400", help = "The raster height") int height,
            @CliOption(key = "z",      mandatory = false, unspecifiedDefaultValue = "-1", specifiedDefaultValue = "0", help = "The zoom level") long z,
            @CliOption(key = "minx",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The min x or column") long minX,
            @CliOption(key = "miny",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The min y or row") long minY,
            @CliOption(key = "maxx",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The max x or column") long maxX,
            @CliOption(key = "maxy",   mandatory = false, unspecifiedDefaultValue = "-1", help = "The max y or row") long maxY
    ) {
        TileLayer tileLayer = catalog.tiles[tileName]
        if (tileLayer) {
            Workspace workspace = catalog.workspaces[workspaceName]
            if (workspace) {
                TileCursor tileCursor
                if (bounds && z == -1) {
                    Bounds b = Bounds.fromString(bounds)
                    if (b.proj && !b.proj.equals(tileLayer.pyramid.proj)) {
                        b = b.reproject(tileLayer.pyramid.proj)
                    }
                    tileCursor = tileLayer.tiles(b, width, height)
                } else if (bounds && z > -1) {
                    Bounds b = Bounds.fromString(bounds)
                    if (b.proj && !b.proj.equals(tileLayer.pyramid.proj)) {
                        b = b.reproject(tileLayer.pyramid.proj)
                    }
                    tileCursor = tileLayer.tiles(b, z)
                } else if (z > -1 && minX > -1 && minY > -1 && maxX > -1 && maxY > -1) {
                    tileCursor = tileLayer.tiles(z, minX, minY, maxX, maxY)
                } else if (z > -1) {
                    tileCursor = tileLayer.tiles(z)
                } else {
                    return "Wrong combination of options!"
                }
                Layer layer = tileLayer.getLayer(tileCursor,
                        outWorkspace: workspace,
                        outLayer: layerName
                )
                catalog.layers[new LayerName(layerName)] = layer
                "Done generating the vector grid ${layerName} from ${tileName}!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Tile Layer ${tileName}"
        }
    }
    
}
