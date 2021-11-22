package org.geoshell.raster

import geoscript.layer.Format
import org.geoshell.Catalog
import org.geoshell.vector.LayerName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.shell.core.Completion

import static org.junit.jupiter.api.Assertions.*

class FormatNameConverterTest {

    @TempDir
    File folder

    @Test
    void supports() {
        FormatNameConverter converter = new FormatNameConverter()
        assertTrue converter.supports(FormatName, "world")
        assertFalse converter.supports(LayerName, "cities")
    }

    @Test
    void convertFromText() {
        FormatNameConverter converter = new FormatNameConverter()
        assertEquals new FormatName("world"), converter.convertFromText("world", FormatName, "")
    }

    @Test
    void getAllPossibleValues() {
        Catalog catalog = new Catalog()
        catalog.formats[new FormatName("world")] = Format.getFormat(new File(folder, "world.tif"))
        catalog.formats[new FormatName("terrain")] = Format.getFormat(new File(folder, "terrain.tif"))

        FormatNameConverter converter = new FormatNameConverter(catalog: catalog)
        List<Completion> completions = []
        assertTrue converter.getAllPossibleValues(completions, FormatName, "", "", null)
        assertTrue new Completion("world") in completions
        assertTrue new Completion("terrain") in completions
    }

}
