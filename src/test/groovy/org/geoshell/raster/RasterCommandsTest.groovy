package org.geoshell.raster

import geoscript.layer.Format
import geoscript.layer.Raster
import org.geoshell.Catalog
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.springframework.shell.support.util.OsUtils

import static org.junit.Assert.*

class RasterCommandsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test void open() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        String result = cmds.open(new FormatName("raster"), new RasterName("raster"), null)
        assertEquals "Opened Format raster Raster raster as raster:raster", result
        assertNotNull catalog.rasters[new RasterName("raster:raster")]
    }

    @Test void close() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        // Open
        cmds.open(new FormatName("raster"), new RasterName("raster"), null)
        assertNotNull catalog.rasters[new RasterName("raster:raster")]
        // Close
        String result = cmds.close(new RasterName("raster:raster"))
        assertEquals "Raster raster:raster closed!", result
        assertNull catalog.rasters[new RasterName("raster:raster")]
    }

    @Test void list() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), null)
        assertNotNull catalog.rasters[new RasterName("raster:raster")]
        String result =  cmds.list()
        assertEquals "raster:raster = GeoTIFF", result
    }

    @Test void info() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), null)
        assertNotNull catalog.rasters[new RasterName("raster:raster")]
        String result = cmds.info(new RasterName("raster:raster"))
        assertTrue result.contains("Format: GeoTIFF")
        assertTrue result.contains("Size: 900, 450")
    }

    @Test void crop() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        File outFile = new File(temporaryFolder.newFolder("cropped"), "cropped.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("cropped")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), null)
        assertNotNull catalog.rasters[new RasterName("raster:raster")]
        String result = cmds.crop(new RasterName("raster:raster"),new FormatName("cropped"),"cropped","0,0,90,90")
        assertEquals "Raster raster:raster cropped to cropped!", result
        Raster raster = catalog.rasters[new RasterName("cropped")]
        assertNotNull raster
        assertEquals 0.0, raster.bounds.minX, 0.01
        assertEquals 0.0, raster.bounds.minY, 0.01
        assertEquals 90.0, raster.bounds.maxX, 0.01
        assertEquals 90.0, raster.bounds.maxY, 0.01
    }

    @Test void reproject() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        File croppedOutFile = new File(temporaryFolder.newFolder("cropped"), "cropped.tif")
        Format croppedOutFormat = Format.getFormat(croppedOutFile)
        catalog.formats[new FormatName("cropped")] = croppedOutFormat

        File outFile = new File(temporaryFolder.newFolder("reprojected"), "reprojected.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("reprojected")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), null)
        cmds.crop(new RasterName("raster:raster"),new FormatName("cropped"),"cropped","-124.771729,45.359865,-116.883545,48.169749")
        String result = cmds.reproject(new RasterName("cropped"),new FormatName("reprojected"),"reprojected","EPSG:3857")
        assertEquals("Raster cropped reprojected to reprojected as EPSG:3857!", result)
        Raster raster = catalog.rasters[new RasterName("reprojected")]
        assertNotNull raster
        assertEquals "EPSG:3857", raster.proj.srs
    }
}