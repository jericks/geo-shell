package org.geoshell.style

import geoscript.layer.GeoTIFF
import geoscript.workspace.Directory
import org.geoshell.Catalog
import org.geoshell.raster.FormatName
import org.geoshell.raster.RasterName
import org.geoshell.vector.LayerCommands
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.springframework.shell.support.util.OsUtils

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
        String result = cmds.createDefaultVector(new LayerName("points"), "blue", 0.5, styleFile)
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

    @Test void createDefaultRasterStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("raster.tif").toURI())
        catalog.formats[new FormatName("raster")] = new GeoTIFF(file)
        catalog.rasters[new RasterName("raster")] = catalog.formats[new FormatName("raster")].read()

        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createDefaultRaster(new RasterName("raster"), 0.5, styleFile)
        assertTrue result.startsWith("Default Raster Style for raster written to")
        assertTrue result.trim().endsWith("style.sld!")
        String styleText = styleFile.text
        assertTrue styleText.contains("<sld:UserLayer>") && styleText.contains("<sld:RasterSymbolizer>")
    }
}
