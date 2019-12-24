package org.geoshell.docs

import org.junit.Test

class TileDocTest extends AbstractDocTest {

    @Test
    void open() {
        run("tile_open", [
                "tile open --name countries --params src/test/resources/countries.mbtiles",
                "tile close --name countries"
        ])
    }

    @Test
    void close() {
        run("tile_close", [
                "tile open --name countries --params src/test/resources/countries.mbtiles",
                "tile close --name countries"
        ])
    }

    @Test
    void list() {
        run("tile_list", [
                "tile open --name countries --params src/test/resources/countries.mbtiles",
                "tile list",
                "tile close --name countries"
        ])
    }

    @Test
    void info() {
        run("tile_info", [
                "tile open --name countries --params src/test/resources/countries.mbtiles",
                "tile info --name countries",
                "tile close --name countries"
        ])
    }


}
