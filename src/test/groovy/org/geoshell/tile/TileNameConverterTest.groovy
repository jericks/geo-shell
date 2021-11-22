package org.geoshell.tile

import geoscript.layer.TileLayer
import org.geoshell.Catalog
import org.geoshell.vector.LayerName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.shell.core.Completion

import static org.junit.jupiter.api.Assertions.*

class TileNameConverterTest {

    @TempDir
    File folder

    @Test
    void supports() {
        TileNameConverter converter = new TileNameConverter()
        assertTrue converter.supports(TileName, "tiles")
        assertFalse converter.supports(LayerName, "cities")
    }

    @Test
    void convertFromText() {
        TileNameConverter converter = new TileNameConverter()
        assertEquals new TileName("tiles"), converter.convertFromText("tiles", TileName, "")
    }

    @Test
    void getAllPossibleValues() {
        Catalog catalog = new Catalog()
        catalog.tiles[new TileName("states")] = TileLayer.getTileLayer(new File(folder, "states.mbtiles").absolutePath)
        catalog.tiles[new TileName("world")] = TileLayer.getTileLayer(new File(folder, "world.gpkg").absolutePath)

        TileNameConverter converter = new TileNameConverter(catalog: catalog)

        List<Completion> completions = []
        assertTrue converter.getAllPossibleValues(completions, TileName, "", "", null)
        assertTrue new Completion("states") in completions
        assertTrue new Completion("world") in completions
    }
    
}
