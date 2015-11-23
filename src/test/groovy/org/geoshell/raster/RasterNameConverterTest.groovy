package org.geoshell.raster

import geoscript.layer.Format
import org.geoshell.Catalog
import org.geoshell.vector.LayerName
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.springframework.shell.core.Completion
import org.springframework.shell.core.MethodTarget

import java.lang.reflect.Method

import static org.junit.Assert.*

class RasterNameConverterTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test
    void supports() {
        RasterNameConverter converter = new RasterNameConverter()
        assertTrue converter.supports(RasterName, "world")
        assertFalse converter.supports(LayerName, "points")
    }

    @Test
    void convertFromText() {
        RasterNameConverter converter = new RasterNameConverter()
        assertEquals new RasterName("terrain"), converter.convertFromText("terrain", RasterName, "")
    }

    @Test
    void getAllPossibleValues() {
        Catalog catalog = new Catalog()
        catalog.formats[new FormatName("raster")] = Format.getFormat(new File(getClass().getClassLoader().getResource("raster.tif").toURI()))
        catalog.formats[new FormatName("earth")] = Format.getFormat(new File(getClass().getClassLoader().getResource("earth.tif").toURI()))
        catalog.rasters[new RasterName("raster")] = catalog.formats[new FormatName("raster")].read("raster")
        catalog.rasters[new RasterName("earth")] = catalog.formats[new FormatName("earth")].read("earth")

        Method method = RasterCommands.class.getDeclaredMethod("open", FormatName, RasterName, String)
        MethodTarget target = new MethodTarget(method, new RasterCommands(), "", "key")

        // No Format
        RasterNameConverter converter = new RasterNameConverter(catalog: catalog)
        List<Completion> completions = []
        assertTrue converter.getAllPossibleValues(completions, RasterName, "", "", target)
        assertTrue new Completion("raster") in completions
        assertTrue new Completion("earth") in completions

        // Raster Format
        target = new MethodTarget(method, new RasterCommands(), "--format raster --raster ", "key")
        converter = new RasterNameConverter(catalog: catalog)
        completions = []
        assertTrue converter.getAllPossibleValues(completions, RasterName, "", "", target)
        assertTrue new Completion("raster") in completions
        assertFalse new Completion("earth") in completions

        // Earth Format
        target = new MethodTarget(method, new RasterCommands(), "--format earth --raster ", "key")
        converter = new RasterNameConverter(catalog: catalog)
        completions = []
        assertTrue converter.getAllPossibleValues(completions, RasterName, "", "", target)
        assertFalse new Completion("raster") in completions
        assertTrue new Completion("earth") in completions
    }
}
