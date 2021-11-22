package org.geoshell.tile

import geoscript.layer.GeoTIFF
import geoscript.layer.Layer
import geoscript.layer.Shapefile
import geoscript.layer.TileLayer
import geoscript.workspace.Memory
import org.geoshell.Catalog
import org.geoshell.map.MapCommands
import org.geoshell.map.MapName
import org.geoshell.raster.FormatName
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.shell.support.util.OsUtils

import static org.junit.jupiter.api.Assertions.*

class TileCommandsTest {

    @TempDir
    File folder

    @Test void open() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        String result = cmds.open(new TileName("world"), file.absolutePath)
        assertEquals "Tile Layer world opened!", result
    }

    @Test void openOsm() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        String result = cmds.open(new TileName("osm"), "type=osm url=http://a.tile.openstreetmap.org")
        assertEquals "Tile Layer osm opened!", result
    }

    @Test void close() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        String result = cmds.close(new TileName("world"))
        assertEquals "Tile Layer world closed!", result
    }

    @Test void list() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        File file1 = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file1.absolutePath)
        File file2 = createFile("states", "states.gpkg")
        cmds.open(new TileName("states"), file2.absolutePath)
        String result = cmds.list()
        assertEquals "world = MBTiles" + OsUtils.LINE_SEPARATOR + "states = GeoPackage", result
    }

    @Test void info() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        String result = cmds.info(new TileName("world"))
        assertTrue result.contains("world")
        assertTrue result.contains("EPSG:3857")
        assertTrue result.contains("BOTTOM_LEFT")
        assertTrue result.contains("256,256")
        assertTrue result.contains("0,1,1,156412.0,156412.0")
    }

    @Test void generate() {
        Catalog catalog = new Catalog()

        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        catalog.layers[new LayerName("grid")] = layer
        MapCommands mapCommands = new MapCommands(catalog: catalog)
        mapCommands.open(new MapName("grid"))
        mapCommands.addLayer(new MapName("grid"), new LayerName("grid"), null)

        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        String result = cmds.generate(new TileName("world"), new MapName("grid"),0,1,null,null,false,false)
        assertEquals "Tiles generated!", result
        TileLayer tilelayer = catalog.tiles[new TileName("world")]
        assertNotNull tilelayer.get(0,0,0).data
        assertNotNull tilelayer.get(1,1,1).data
    }

    @Test void generateWithMetatiles() {
        Catalog catalog = new Catalog()

        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        catalog.layers[new LayerName("grid")] = layer
        MapCommands mapCommands = new MapCommands(catalog: catalog)
        mapCommands.open(new MapName("grid"))
        mapCommands.addLayer(new MapName("grid"), new LayerName("grid"), null)

        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        String result = cmds.generate(new TileName("world"), new MapName("grid"),0,2,null,"4,4",false,false)
        assertEquals "Tiles generated!", result
        TileLayer tilelayer = catalog.tiles[new TileName("world")]
        assertNotNull tilelayer.get(0,0,0).data
        assertNotNull tilelayer.get(1,1,1).data
    }

    @Test void delete() {
        Catalog catalog = new Catalog()

        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        catalog.layers[new LayerName("grid")] = layer
        MapCommands mapCommands = new MapCommands(catalog: catalog)
        mapCommands.open(new MapName("grid"))
        mapCommands.addLayer(new MapName("grid"), new LayerName("grid"), null)

        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        String result = cmds.generate(new TileName("world"), new MapName("grid"),0,1,null,null,false,false)
        assertEquals "Tiles generated!", result
        TileLayer tilelayer = catalog.tiles[new TileName("world")]
        assertNotNull tilelayer.get(0,0,0).data
        assertNotNull tilelayer.get(1,1,1).data

        // Delete a Tile
        result = cmds.delete(new TileName("world"), "1/1/1", null, -1, -1, -1, -1, -1, -1, -1)
        assertEquals "Deleting tile 1/1/1", result
        assertNull tilelayer.get(1,1,1).data
        // Delete a zoom level
        result = cmds.delete(new TileName("world"), null, null, -1, -1, 1, -1, -1, -1, -1)
        assertEquals "Deleting tiles at z level 1", result
        assertNull tilelayer.get(1,1,2).data
    }

    @Test void stitchRaster() {
        Catalog catalog = new Catalog()

        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        catalog.layers[new LayerName("grid")] = layer
        MapCommands mapCommands = new MapCommands(catalog: catalog)
        mapCommands.open(new MapName("grid"))
        mapCommands.addLayer(new MapName("grid"), new LayerName("grid"), null)

        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        cmds.generate(new TileName("world"), new MapName("grid"),0,2,null,null,false,false)

        catalog.formats[new FormatName("osm1")] = new GeoTIFF(new File(folder, "osm1.tif"))
        catalog.formats[new FormatName("osm2")] = new GeoTIFF(new File(folder, "osm2.tif"))
        catalog.formats[new FormatName("osm3")] = new GeoTIFF(new File(folder, "osm3.tif"))
        catalog.formats[new FormatName("osm4")] = new GeoTIFF(new File(folder, "osm4.tif"))

        // Zoom Level
        String result = cmds.stitchRaster(new TileName("world"), new FormatName("osm1"), "osm1", null, 400, 400, 1, -1, -1, -1, -1)
        assertEquals "Done stitching Raster osm1 from world!", result

        // Bounds and Zoom Level
        result = cmds.stitchRaster(new TileName("world"), new FormatName("osm2"), "osm2", "-128.803711,44.134913,-113.038330,49.731581,EPSG:4326", 400, 400, 1, -1, -1, -1, -1)
        assertEquals "Done stitching Raster osm2 from world!", result

        // Bounds
        result = cmds.stitchRaster(new TileName("world"), new FormatName("osm3"), "osm3", "-170,-80,170,80,EPSG:4326", 400, 400, -1, -1, -1, -1, -1)
        assertEquals "Done stitching Raster osm3 from world!", result

        // Column and Row
        result = cmds.stitchRaster(new TileName("world"), new FormatName("osm4"), "osm4", null, 400, 400, 2, 0, 0, 2, 2)
        assertEquals "Done stitching Raster osm4 from world!", result
    }

    @Test void vectorGrid() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("workspace")] = new Memory()

        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)

        // Generate zoom level 0 and 1
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        catalog.layers[new LayerName("grid")] = layer
        MapCommands mapCommands = new MapCommands(catalog: catalog)
        mapCommands.open(new MapName("grid"))
        mapCommands.addLayer(new MapName("grid"), new LayerName("grid"), null)
        cmds.generate(new TileName("world"), new MapName("grid"),0,2,null,null,false,false)

        // Zoom Level
        String result = cmds.vectorGrid(new TileName("world"), new WorkspaceName("workspace"), "grid_z2", null, 400, 400, 2, -1, -1, -1, -1)
        assertEquals "Done generating the vector grid grid_z2 from world!", result
        Layer gridLayer = catalog.layers[new LayerName("grid_z2")]
        assertNotNull gridLayer
        assertEquals 16, gridLayer.count

        // Bounds and Zoom Level
        result = cmds.vectorGrid(new TileName("world"), new WorkspaceName("workspace"), "grid_b_z", "-128.803711,44.134913,-113.038330,49.731581,EPSG:4326", 400, 400, 1, -1, -1, -1, -1)
        assertEquals "Done generating the vector grid grid_b_z from world!", result
        gridLayer = catalog.layers[new LayerName("grid_b_z")]
        assertNotNull gridLayer
        assertEquals 1, gridLayer.count

        // Bounds
        result = cmds.vectorGrid(new TileName("world"), new WorkspaceName("workspace"), "grid_b", "-170,-80,170,80,EPSG:4326", 400, 400, -1, -1, -1, -1, -1)
        assertEquals "Done generating the vector grid grid_b from world!", result
        gridLayer = catalog.layers[new LayerName("grid_b")]
        assertNotNull gridLayer
        assertEquals 4, gridLayer.count

        // Column and Row
        result = cmds.vectorGrid(new TileName("world"), new WorkspaceName("workspace"), "grid_cr", null, 400, 400, 2, 0, 0, 2, 2)
        assertEquals "Done generating the vector grid grid_cr from world!", result
        gridLayer = catalog.layers[new LayerName("grid_cr")]
        assertNotNull gridLayer
        assertEquals 9, gridLayer.count
    }

    @Test void tiles() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = createFile("world", "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        String result = cmds.tiles(new TileName("world"), "2315277.538707974,4356146.199006655,2534193.2172859586,4470343.227121928", 10)
        assertEquals """10/571/623
10/572/623
10/573/623
10/574/623
10/575/623
10/576/623
10/571/624
10/572/624
10/573/624
10/574/624
10/575/624
10/576/624
10/571/625
10/572/625
10/573/625
10/574/625
10/575/625
10/576/625
10/571/626
10/572/626
10/573/626
10/574/626
10/575/626
10/576/626
""".denormalize(), result
    }

    private File createFile(String dirName, String fileName) {
        File dir = new File(folder, dirName)
        dir.mkdir()
        return new File(dir, fileName)
    }
}
