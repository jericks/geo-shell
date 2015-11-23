package org.geoshell.raster

import org.geoshell.Catalog
import org.junit.Test
import org.springframework.shell.support.util.OsUtils

import static org.junit.Assert.*

class FormatCommandsTest {

    @Test void open() {
        Catalog catalog = new Catalog()
        FormatCommands cmds = new FormatCommands(catalog: catalog)
        String result = cmds.open(new FormatName("raster"), new File(getClass().getClassLoader().getResource("raster.tif").toURI()).absolutePath)
        assertEquals "Format raster opened!", result
        assertNotNull catalog.formats[new FormatName("raster")]
    }

    @Test void close() {
        Catalog catalog = new Catalog()
        FormatCommands cmds = new FormatCommands(catalog: catalog)
        // Open
        String result = cmds.open(new FormatName("raster"), new File(getClass().getClassLoader().getResource("raster.tif").toURI()).absolutePath)
        assertEquals "Format raster opened!", result
        assertNotNull catalog.formats[new FormatName("raster")]
        // Close
        result = cmds.close(new FormatName("raster"))
        assertEquals "Format raster closed!", result
        assertNull catalog.formats[new FormatName("raster")]
    }

    @Test void list() {
        Catalog catalog = new Catalog()
        FormatCommands cmds = new FormatCommands(catalog: catalog)
        // Open
        cmds.open(new FormatName("raster"), new File(getClass().getClassLoader().getResource("raster.tif").toURI()).absolutePath)
        cmds.open(new FormatName("earth"), new File(getClass().getClassLoader().getResource("earth.tif").toURI()).absolutePath)
        String result = cmds.list()
        assertEquals "raster = GeoTIFF" + OsUtils.LINE_SEPARATOR + "earth = GeoTIFF", result
    }

    @Test void rasters() {
        Catalog catalog = new Catalog()
        FormatCommands cmds = new FormatCommands(catalog: catalog)
        // Open
        cmds.open(new FormatName("raster"), new File(getClass().getClassLoader().getResource("raster.tif").toURI()).absolutePath)
        cmds.open(new FormatName("earth"), new File(getClass().getClassLoader().getResource("earth.tif").toURI()).absolutePath)
        assertEquals "raster", cmds.rasters(new FormatName("raster"))
        assertEquals "earth", cmds.rasters(new FormatName("earth"))
    }



}
