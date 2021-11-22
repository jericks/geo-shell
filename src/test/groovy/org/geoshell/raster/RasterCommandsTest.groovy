package org.geoshell.raster

import geoscript.layer.Format
import geoscript.layer.Layer
import geoscript.layer.Raster
import geoscript.workspace.Memory
import org.geoshell.Catalog
import org.geoshell.style.StyleCommands
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.junit.jupiter.api.Assertions.*

class RasterCommandsTest {

    @TempDir
    File folder

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

    @Test void value() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), null)
        String result = cmds.value(new RasterName("raster:raster"), 0, -179, 89, "geometry")
        assertEquals "184.0", result
        result = cmds.value(new RasterName("raster:raster"), 0, 10, 15, "pixel")
        assertEquals "184.0", result
    }

    @Test void crop() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        File outFile = createFile("cropped", "cropped.tif")
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

        File croppedOutFile = createFile("cropped", "cropped.tif")
        Format croppedOutFormat = Format.getFormat(croppedOutFile)
        catalog.formats[new FormatName("cropped")] = croppedOutFormat

        File outFile = createFile("reprojected", "reprojected.tif")
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

    @Test void getSetStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")

        File sldFile = new File(folder, "raster.sld")

        String result = cmds.getStyle(new RasterName("raster"), sldFile)
        assertTrue result.startsWith("raster style written to")
        assertTrue result.endsWith("raster.sld")

        result = cmds.getStyle(new RasterName("raster"), null)
        assertTrue result.contains("<sld:StyledLayerDescriptor")

        result = cmds.setStyle(new RasterName("raster"), sldFile)
        assertTrue result.startsWith("Style ")
        assertTrue result.endsWith("raster.sld set on raster")
    }

    @Test void contours() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.contours(new RasterName("raster"), new WorkspaceName("mem"), "raster_contours", 0, "184,185,186,187", false, false, "")
        assertEquals("Done creating contours!", result)
        Layer layer = catalog.layers[new LayerName("raster_contours")]
        assertNotNull layer
    }

    @Test void envelope() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.envelope(new RasterName("raster"), new WorkspaceName("mem"), "envelope")
        assertEquals("Done creating envelope in envelope from raster!", result)
        Layer layer = catalog.layers[new LayerName("envelope")]
        assertNotNull layer
        assertEquals 1, layer.count
    }

    @Test void reclassify() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format

        File outFile = createFile("reclassified", "reclassified.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("reclassified")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.reclassify(new RasterName("raster"), new FormatName("reclassified"), "reclassified", "0-185=1,186-200=2,201-255=3", 0, 0)
        assertEquals("Raster raster reclassified to reclassified!", result)
        Raster raster = catalog.rasters[new RasterName("reclassified")]
        assertNotNull raster
    }

    @Test void scale() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format
        Raster inRaster = format.read()

        File outFile = createFile("scaled", "scaled.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("scaled")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.scale(new RasterName("raster"), new FormatName("scaled"), "scaled", 2, 3, 0, 0, "nearest")
        assertEquals("Raster raster scaled to scaled!", result)
        Raster outRaster = catalog.rasters[new RasterName("scaled")]
        assertNotNull outRaster
        assertTrue(inRaster.pixelSize[0] > outRaster.pixelSize[0])
        assertTrue(inRaster.pixelSize[1] > outRaster.pixelSize[1])
    }

    @Test void addConstant() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format
        Raster inRaster = format.read()

        File outFile = createFile("added", "added.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("added")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.addConstant(new RasterName("raster"), new FormatName("added"), "added", "10")
        assertEquals("Added 10 to raster to create added!", result)
        Raster outRaster = catalog.rasters[new RasterName("added")]
        assertNotNull outRaster
        assertEquals(inRaster.getValue(0,0,0)   + 10, outRaster.getValue(0,0,0),  0.1)
        assertEquals(inRaster.getValue(10,10,0) + 10, outRaster.getValue(10,10,0), 0.1)
        assertEquals(inRaster.getValue(30,20,0) + 10, outRaster.getValue(30,20,0), 0.1)
    }

    @Test void addRasters() {
        Catalog catalog = new Catalog()

        // Raster 1
        Format format1 = Format.getFormat(new File(getClass().getClassLoader().getResource("five.tif").toURI()))
        catalog.formats[new FormatName("raster1")] = format1
        Raster raster1 = format1.read()

        // Raster 2
        Format format2 = Format.getFormat(new File(getClass().getClassLoader().getResource("ten.tif").toURI()))
        catalog.formats[new FormatName("raster2")] = format2
        Raster raster2 = format2.read()

        // Output
        File outFile = createFile("added", "added.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("added")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster1"), new RasterName("five"), "raster1")
        cmds.open(new FormatName("raster2"), new RasterName("ten"), "raster2")
        String result = cmds.addRasters(new RasterName("raster1"), new RasterName("raster2"), new FormatName("added"), "added")
        assertEquals("Added raster1 to raster2 to create added!", result)
        Raster outRaster = catalog.rasters[new RasterName("added")]
        assertNotNull outRaster
        assertEquals(15, outRaster.getValue(0,0,0),  0.1)
        assertEquals(15, outRaster.getValue(100,100,0),  0.1)
        assertEquals(15, outRaster.getValue(200,200,0),  0.1)
    }

    @Test void subtractConstant() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format
        Raster inRaster = format.read()

        File outFile = createFile("subtracted", "subtracted.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("subtracted")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.subtractConst(new RasterName("raster"), new FormatName("subtracted"), "subtracted", "10", false)
        assertEquals("Subtracted 10 from raster to create subtracted!", result)
        Raster outRaster = catalog.rasters[new RasterName("subtracted")]
        assertNotNull outRaster
        assertEquals(inRaster.getValue(0,0,0)   - 10, outRaster.getValue(0,0,0),  0.1)
        assertEquals(inRaster.getValue(10,10,0) - 10, outRaster.getValue(10,10,0), 0.1)
        assertEquals(inRaster.getValue(30,20,0) - 10, outRaster.getValue(30,20,0), 0.1)
    }

    @Test void subtractRasters() {
        Catalog catalog = new Catalog()

        // Raster 1
        Format format1 = Format.getFormat(new File(getClass().getClassLoader().getResource("ten.tif").toURI()))
        catalog.formats[new FormatName("raster1")] = format1
        Raster raster1 = format1.read()

        // Raster 2
        Format format2 = Format.getFormat(new File(getClass().getClassLoader().getResource("five.tif").toURI()))
        catalog.formats[new FormatName("raster2")] = format2
        Raster raster2 = format2.read()

        // Output
        File outFile = createFile("subtracted", "subtracted.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("subtracted")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster1"), new RasterName("ten"), "raster1")
        cmds.open(new FormatName("raster2"), new RasterName("five"), "raster2")
        String result = cmds.subtractRasters(new RasterName("raster1"), new RasterName("raster2"), new FormatName("subtracted"), "subtracted")
        assertEquals("Subtracted raster1 from raster2 to create subtracted!", result)
        Raster outRaster = catalog.rasters[new RasterName("subtracted")]
        assertNotNull outRaster
        assertEquals(5, outRaster.getValue(0,0,0),  0.1)
        assertEquals(5, outRaster.getValue(100,100,0),  0.1)
        assertEquals(5, outRaster.getValue(200,200,0),  0.1)
    }

    @Test void subtractFromConstant() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format
        Raster inRaster = format.read()

        File outFile = createFile("subtracted", "subtracted.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("subtracted")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.subtractConst(new RasterName("raster"), new FormatName("subtracted"), "subtracted", "255", true)
        assertEquals("Subtracted raster from 255 to create subtracted!", result)
        Raster outRaster = catalog.rasters[new RasterName("subtracted")]
        assertNotNull outRaster
        assertEquals(255 - inRaster.getValue(0,0,0), outRaster.getValue(0,0,0),  0.1)
        assertEquals(255 - inRaster.getValue(10,10,0), outRaster.getValue(10,10,0), 0.1)
        assertEquals(255 - inRaster.getValue(30,20,0), outRaster.getValue(30,20,0), 0.1)
    }

    @Test void multiplyConstant() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format
        Raster inRaster = format.read()

        File outFile = createFile("multiply", "multiply.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("multiply")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.multiplyConstant(new RasterName("raster"), new FormatName("multiply"), "multiply", "1.25")
        assertEquals("Multiplied raster by 1.25 to create multiply!", result)
        Raster outRaster = catalog.rasters[new RasterName("multiply")]
        assertNotNull outRaster
        assertEquals(inRaster.getValue(0,0,0)   * 1.25, outRaster.getValue(0,0,0),  0.1)
        assertEquals(inRaster.getValue(10,10,0) * 1.25, outRaster.getValue(10,10,0), 0.1)
        assertEquals(inRaster.getValue(30,20,0) * 1.25, outRaster.getValue(30,20,0), 0.1)
    }

    @Test void multiplyRasters() {
        Catalog catalog = new Catalog()

        // Raster 1
        Format format1 = Format.getFormat(new File(getClass().getClassLoader().getResource("ten.tif").toURI()))
        catalog.formats[new FormatName("raster1")] = format1
        Raster raster1 = format1.read()

        // Raster 2
        Format format2 = Format.getFormat(new File(getClass().getClassLoader().getResource("five.tif").toURI()))
        catalog.formats[new FormatName("raster2")] = format2
        Raster raster2 = format2.read()

        // Output
        File outFile = createFile("multiplied", "multiplied.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("multiplied")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster1"), new RasterName("ten"), "raster1")
        cmds.open(new FormatName("raster2"), new RasterName("five"), "raster2")
        String result = cmds.multiplyRasters(new RasterName("raster1"), new RasterName("raster2"), new FormatName("multiplied"), "multiplied")
        assertEquals("Multiplied raster1 and raster2 to create multiplied!", result)
        Raster outRaster = catalog.rasters[new RasterName("multiplied")]
        assertNotNull outRaster
        assertEquals(50, outRaster.getValue(0,0,0),  0.1)
        assertEquals(50, outRaster.getValue(100,100,0),  0.1)
        assertEquals(50, outRaster.getValue(200,200,0),  0.1)
    }

    @Test void divideConstant() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format
        Raster inRaster = format.read()

        File outFile = createFile("divide", "divide.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("divide")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        String result = cmds.divideConstant(new RasterName("raster"), new FormatName("divide"), "divide", "2")
        assertEquals("Divided raster by 2 to create divide!", result)
        Raster outRaster = catalog.rasters[new RasterName("divide")]
        assertNotNull outRaster
        assertEquals(inRaster.getValue(0,0,0)   / 2, outRaster.getValue(0,0,0),  0.1)
        assertEquals(inRaster.getValue(10,10,0) / 2, outRaster.getValue(10,10,0), 0.1)
        assertEquals(inRaster.getValue(30,20,0) / 2, outRaster.getValue(30,20,0), 0.1)
    }

    @Test void divideRasters() {
        Catalog catalog = new Catalog()

        // Raster 1
        Format format1 = Format.getFormat(new File(getClass().getClassLoader().getResource("ten.tif").toURI()))
        catalog.formats[new FormatName("raster1")] = format1
        Raster raster1 = format1.read()

        // Raster 2
        Format format2 = Format.getFormat(new File(getClass().getClassLoader().getResource("five.tif").toURI()))
        catalog.formats[new FormatName("raster2")] = format2
        Raster raster2 = format2.read()

        // Output
        File outFile = createFile("divided", "divided.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("divided")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster1"), new RasterName("ten"), "raster1")
        cmds.open(new FormatName("raster2"), new RasterName("five"), "raster2")
        String result = cmds.divideRasters(new RasterName("raster1"), new RasterName("raster2"), new FormatName("divided"), "divided")
        assertEquals("Divided raster1 by raster2 to create divided!", result)
        Raster outRaster = catalog.rasters[new RasterName("divided")]
        assertNotNull outRaster
        assertEquals(2, outRaster.getValue(0,0,0),  0.1)
        assertEquals(2, outRaster.getValue(100,100,0),  0.1)
        assertEquals(2, outRaster.getValue(200,200,0),  0.1)
    }

    @Test void stylize() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format
        catalog.rasters[new RasterName("raster")] = catalog.formats[new FormatName("raster")].read()

        File outFile = createFile("stylized", "stylized.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("stylized")] = outFormat

        StyleCommands styleCommands = new StyleCommands(catalog: catalog)
        File styleFile = new File(folder, "style.sld")
        styleCommands.createColorMapRasterStyle(new RasterName("raster"), 0.5, "10=red,50=blue,100=wheat,250=white", "ramp", false, styleFile)

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")
        cmds.setStyle(new RasterName("raster"), styleFile)

        String result = cmds.stylize(new RasterName("raster"), new FormatName("stylized"), "stylized")
        assertEquals("Stylized raster to create stylized!", result)
        Raster outRaster = catalog.rasters[new RasterName("stylized")]
        assertNotNull outRaster
        assertEquals(214.0, outRaster.getValue(0,0,0),  0.1)
        assertEquals(214.0, outRaster.getValue(10,10,0), 0.1)
        assertEquals(212.0, outRaster.getValue(30,20,0), 0.1)
    }

    @Test void shadedRelief() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        Format format = Format.getFormat(file)
        catalog.formats[new FormatName("raster")] = format
        catalog.rasters[new RasterName("raster")] = catalog.formats[new FormatName("raster")].read()

        File outFile = createFile("shaded", "shaded.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("shaded")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster"), new RasterName("raster"), "raster")

        String result = cmds.shadedRelief(new RasterName("raster"), new FormatName("shaded"), "shaded", 1.0, 15.0, 15.0, 0.5, 0.5, 1.0, "DEFAULT")
        assertEquals("Create shaded relief shaded from raster!", result)
        Raster outRaster = catalog.rasters[new RasterName("shaded")]
        assertNotNull outRaster
        assertEquals(67.0, outRaster.getValue(0,0,0),  0.1)
        assertEquals(67.0, outRaster.getValue(10,10,0), 0.1)
        assertEquals(1.0, outRaster.getValue(30,20,0), 0.1)
    }

    @Test void mosaic() {

        Catalog catalog = new Catalog()
        File file1 = new File(getClass().getClassLoader().getResource("mosaic/alki2.tif").toURI())
        Format format1 = Format.getFormat(file1)
        catalog.formats[new FormatName("raster1")] = format1
        catalog.rasters[new RasterName("raster1")] = catalog.formats[new FormatName("raster1")].read()

        File file2 = new File(getClass().getClassLoader().getResource("mosaic/alki3.tif").toURI())
        Format format2 = Format.getFormat(file2)
        catalog.formats[new FormatName("raster2")] = format2
        catalog.rasters[new RasterName("raster2")] = catalog.formats[new FormatName("raster2")].read()

        File outFile = createFile("mosaic", "mosaic.tif")
        Format outFormat = Format.getFormat(outFile)
        catalog.formats[new FormatName("mosaic")] = outFormat

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("raster1"), new RasterName("raster1"), "raster1")
        cmds.open(new FormatName("raster2"), new RasterName("raster2"), "raster2")

        String result = cmds.mosaic(new RasterName("raster1"), new RasterName("raster2"), new FormatName("mosaic"), "mosaic")
        assertEquals("Mosaic raster1 and raster2 to create mosaic!", result)
        Raster outRaster = catalog.rasters[new RasterName("mosaic")]
        assertNotNull outRaster
    }

    @Test void polygon() {
        Catalog catalog = new Catalog()
        File highFile = new File(getClass().getClassLoader().getResource("high.tif").toURI())
        Format highFormat = Format.getFormat(highFile)
        catalog.formats[new FormatName("high")] = highFormat
        catalog.workspaces[new WorkspaceName("layers")] = new Memory()

        RasterCommands cmds = new RasterCommands(catalog: catalog)
        cmds.open(new FormatName("high"), new RasterName("high"), "high")

        String result = cmds.polygon(new RasterName("high"), new WorkspaceName("layers"), "polygons", 0, true, null, 0, null)
        assertEquals "Done converting Raster high to a Polygon Layer polygons!", result
        Layer layer = catalog.layers[new LayerName("polygons")]
        assertNotNull layer
        assertEquals 16, layer.count()
    }

    private File createFile(String dirName, String fileName) {
        File dir = new File(folder, dirName)
        dir.mkdir()
        return new File(dir, fileName)
    }

}