package org.geoshell.style

import geoscript.layer.GeoTIFF
import geoscript.workspace.Directory
import org.geoshell.Catalog
import org.geoshell.raster.FormatName
import org.geoshell.raster.RasterName
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.*

class StyleCommandsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    @Test void create() {
        Catalog catalog = new Catalog()
        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File file = folder.newFile("style.sld")
        String result = cmds.create("stroke=black", file)
        assertTrue result.startsWith("Style stroke=black written to")
        assertTrue result.trim().endsWith("style.sld!")
        assertTrue file.text.contains("<sld:UserLayer>")
    }

    @Test void createDefaultVectorStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("points.shp").toURI())
        catalog.workspaces[new WorkspaceName("shps")] = new Directory(file.parentFile)
        catalog.layers[new LayerName("points")] = catalog.workspaces[new WorkspaceName("shps")].get("points")

        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createDefaultVectorStyle(new LayerName("points"), "blue", 0.5, styleFile)
        assertTrue result.startsWith("Default Vector Style for points written to")
        assertTrue result.trim().endsWith("style.sld!")
        assertTrue styleFile.text.contains("<sld:UserLayer>")
    }

    @Test void createUniqueValuesVectorStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("grid.shp").toURI())
        catalog.workspaces[new WorkspaceName("shps")] = new Directory(file.parentFile)
        catalog.layers[new LayerName("grid")] = catalog.workspaces[new WorkspaceName("shps")].get("grid")

        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createUniqueValuesVectorStyle(new LayerName("grid"), "col", "random", styleFile)
        assertTrue result.startsWith("Unique Values Vector Style for grid's col Field written to")
        assertTrue result.trim().endsWith("style.sld!")
        assertTrue styleFile.text.contains("<sld:UserLayer>")
    }

    @Test void createUniqueValuesStyleFromText() {

        String text = """AHa=#aa0c74
AHat=#b83b1f
AHcf=#964642
AHh=#78092e
AHpe=#78092e
AHt=#5f025a
AHt3=#e76161
Aa1=#fcedcd
Aa2=#94474b"""
        File textFile = folder.newFile("unique.txt")
        textFile.text = text

        Catalog catalog = new Catalog()
        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createUniqueValuesStyleFromText("units", "polygon", textFile, styleFile)
        assertTrue result.startsWith("Create a unique values style from")
        assertTrue result.contains("for units and polygon to")
        assertTrue result.endsWith("style.sld")
        assertTrue styleFile.text.contains("<sld:UserLayer>")
    }

    @Test void createGradientVectorStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("grid.shp").toURI())
        catalog.workspaces[new WorkspaceName("shps")] = new Directory(file.parentFile)
        catalog.layers[new LayerName("grid")] = catalog.workspaces[new WorkspaceName("shps")].get("grid")

        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createGradientVectorStyle(new LayerName("grid"), "col", 8, "reds", "Quantile", "ignore", styleFile)
        assertTrue result.startsWith("Gradient Vector Style for grid's col Field written to")
        assertTrue result.trim().endsWith("style.sld!")
        assertTrue styleFile.text.contains("<sld:UserLayer>")
    }

    @Test void createDefaultRasterStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        catalog.formats[new FormatName("raster")] = new GeoTIFF(file)
        catalog.rasters[new RasterName("raster")] = catalog.formats[new FormatName("raster")].read()

        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createDefaultRasterStyle(new RasterName("raster"), 0.5, styleFile)
        assertTrue result.startsWith("Default Raster Style for raster written to")
        assertTrue result.trim().endsWith("style.sld!")
        String styleText = styleFile.text
        assertTrue styleText.contains("<sld:UserLayer>") && styleText.contains("<sld:RasterSymbolizer>")
    }

    @Test void createColorMapRasterStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        catalog.formats[new FormatName("raster")] = new GeoTIFF(file)
        catalog.rasters[new RasterName("raster")] = catalog.formats[new FormatName("raster")].read()

        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createColorMapRasterStyle(new RasterName("raster"), 0.5, "10=red,50=blue,100=wheat,250=white", "ramp", false, styleFile)
        assertTrue result.startsWith("Colormap Raster Style for raster written to")
        assertTrue result.trim().endsWith("style.sld!")
        String styleText = styleFile.text
        assertTrue styleText.contains("<sld:UserLayer>") &&
                styleText.contains("<sld:RasterSymbolizer>") &&
                styleText.contains("<sld:ColorMap>") &&
                styleText.contains("<sld:ColorMapEntry")
    }

    @Test void createColorMapPaletteRasterStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        catalog.formats[new FormatName("raster")] = new GeoTIFF(file)
        catalog.rasters[new RasterName("raster")] = catalog.formats[new FormatName("raster")].read()

        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createColorMapPaletteRasterStyle(1,255, "MutedTerrain", 25, "ramp", false, 1.0, styleFile)
        assertTrue result.startsWith("Colormap Palette Raster Style written to")
        assertTrue result.trim().endsWith("style.sld!")
        String styleText = styleFile.text
        assertTrue styleText.contains("<sld:UserLayer>") &&
                styleText.contains("<sld:RasterSymbolizer>") &&
                styleText.contains("<sld:ColorMap>") &&
                styleText.contains("<sld:ColorMapEntry")
    }

    @Test void saveToStyleRepository() {
        Catalog catalog = new Catalog()
        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        cmds.createColorMapPaletteRasterStyle(1,255, "MutedTerrain", 25, "ramp", false, 1.0, styleFile)
        File databaseFile = folder.newFile("styles.db")
        String result = cmds.saveStyleToStyleRepository("sqlite","file=${databaseFile.absolutePath}", "raster", "raster_colormap", styleFile)
        assertEquals("Style raster_colormap for Layer raster saved to sqlite", result);
    }

    @Test void getFromStyleRepository() {
        Catalog catalog = new Catalog()
        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        cmds.createColorMapPaletteRasterStyle(1,255, "MutedTerrain", 25, "ramp", false, 1.0, styleFile)
        File databaseFile = folder.newFile("styles.db")
        cmds.saveStyleToStyleRepository("sqlite","file=${databaseFile.absolutePath}", "raster", "raster_colormap", styleFile)
        File outputFile = folder.newFile("styles.sld")
        String result = cmds.getStyleFromStyleRepository("sqlite", "file=${databaseFile.absolutePath}", "raster", "raster_colormap", outputFile)
        assertEquals("Style raster_colormap for Layer raster saved to styles.sld", result)
        result = cmds.getStyleFromStyleRepository("sqlite", "file=${databaseFile.absolutePath}", "raster", "raster_colormap", null)
        assertEquals(styleFile.text, result)
    }

    @Test void deleteFromStyleRepository() {
        Catalog catalog = new Catalog()
        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        cmds.createColorMapPaletteRasterStyle(1,255, "MutedTerrain", 25, "ramp", false, 1.0, styleFile)
        File databaseFile = folder.newFile("styles.db")
        cmds.saveStyleToStyleRepository("sqlite","file=${databaseFile.absolutePath}", "raster", "raster_colormap", styleFile)
        String result = cmds.listStylesInStyleRepository("sqlite", "file=${databaseFile.absolutePath}", null)
        assertTrue(result.contains("raster raster_colormap"))
        result = cmds.deleteStyleFromStyleRepository("sqlite", "file=${databaseFile.absolutePath}", "raster", "raster_colormap")
        assertEquals("Style raster_colormap for Layer raster deleted from sqlite", result)
        result = cmds.listStylesInStyleRepository("sqlite", "file=${databaseFile.absolutePath}", null)
        assertFalse(result.contains("raster raster_colormap"))
    }

    @Test void listStylesInStyleRepository() {
        Catalog catalog = new Catalog()
        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        cmds.createColorMapPaletteRasterStyle(1,255, "MutedTerrain", 25, "ramp", false, 1.0, styleFile)
        File databaseFile = folder.newFile("styles.db")
        cmds.saveStyleToStyleRepository("sqlite","file=${databaseFile.absolutePath}", "raster", "raster_colormap", styleFile)
        String result = cmds.listStylesInStyleRepository("sqlite", "file=${databaseFile.absolutePath}", null)
        assertTrue(result.contains("raster raster_colormap"))
        result = cmds.listStylesInStyleRepository("sqlite", "file=${databaseFile.absolutePath}", "raster")
        assertTrue(result.contains("raster raster_colormap"))
    }

    @Test void copyStylesInStyleRepository() {
        Catalog catalog = new Catalog()
        StyleCommands cmds = new StyleCommands(catalog: catalog)

        File styleFile1 = folder.newFile("style1.sld")
        cmds.createColorMapPaletteRasterStyle(1,255, "MutedTerrain", 25, "ramp", false, 1.0, styleFile1)
        File styleFile2 = folder.newFile("style2.sld")
        cmds.createColorMapPaletteRasterStyle(1,255, "Blues", 25, "ramp", false, 1.0, styleFile2)

        File databaseFile = folder.newFile("styles.db")
        cmds.saveStyleToStyleRepository("sqlite","file=${databaseFile.absolutePath}", "raster", "raster_colormap1", styleFile1)
        cmds.saveStyleToStyleRepository("sqlite","file=${databaseFile.absolutePath}", "raster", "raster_colormap2", styleFile2)

        File directory = folder.newFolder("my-styles")
        String result = cmds.copyStylesInStyleRepository("sqlite", "file=${databaseFile.absolutePath}", "nested-directory", "file=${directory.absolutePath}")
        assertEquals("Copy styles from sqlite to nested-directory", result)

        result = cmds.listStylesInStyleRepository("nested-directory", "file=${directory.absolutePath}", null)
        assertTrue(result.contains("raster_colormap1"))
        assertTrue(result.contains("raster_colormap2"))
    }

}
