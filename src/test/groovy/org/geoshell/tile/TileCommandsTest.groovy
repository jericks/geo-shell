package org.geoshell.tile

import geoscript.layer.Layer
import geoscript.layer.Shapefile
import geoscript.layer.TileLayer
import org.geoshell.Catalog
import org.geoshell.map.MapCommands
import org.geoshell.map.MapName
import org.geoshell.vector.LayerName
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.springframework.shell.support.util.OsUtils

import static org.junit.Assert.*

class TileCommandsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test void open() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = new File(temporaryFolder.newFolder("world"), "world.mbtiles")
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
        File file = new File(temporaryFolder.newFolder("world"), "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        String result = cmds.close(new TileName("world"))
        assertEquals "Tile Layer world closed!", result
    }

    @Test void list() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        File file1 = new File(temporaryFolder.newFolder("world"), "world.mbtiles")
        cmds.open(new TileName("world"), file1.absolutePath)
        File file2 = new File(temporaryFolder.newFolder("states"), "states.gpkg")
        cmds.open(new TileName("states"), file2.absolutePath)
        String result = cmds.list()
        assertEquals "world = MBTiles" + OsUtils.LINE_SEPARATOR + "states = GeoPackage", result
    }

    @Test void info() {
        Catalog catalog = new Catalog()
        TileCommands cmds = new TileCommands(catalog: catalog)
        File file = new File(temporaryFolder.newFolder("world"), "world.mbtiles")
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
        File file = new File(temporaryFolder.newFolder("world"), "world.mbtiles")
        cmds.open(new TileName("world"), file.absolutePath)
        String result = cmds.generate(new TileName("world"), new MapName("grid"),0,1,null,false,false)
        assertEquals "Tiles generated!", result
        TileLayer tilelayer = catalog.tiles[new TileName("world")]
        assertNotNull tilelayer.get(0,0,0).data
        assertNotNull tilelayer.get(1,1,1).data
    }

}
