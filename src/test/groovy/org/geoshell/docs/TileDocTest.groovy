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

    @Test
    void vectorGrid() {
        run("tile_vector_grid", [
                "tile open --name countries --params src/test/resources/countries.mbtiles",
                "workspace open --name layers --params memory",
                "tile vector grid --name countries --workspace layers --layer level3 --z 3",
                "style vector default --layer level3 --color #ffffff --opacity 0.25 --file examples/level3.sld",
                "layer style set --name level3 --style examples/level3.sld",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name vectorGridMap",
                "map add layer --name vectorGridMap --layer ocean",
                "map add layer --name vectorGridMap --layer countries",
                "map add layer --name vectorGridMap --layer level3",
                "map draw --name vectorGridMap --file examples/tile_vector_grid.png --projection EPSG:3857 --width 400 --height 400 --bounds -20026376.39,-20048966.10,20026376.39,20048966.10",
                "map close --name vectorGridMap"
        ])
        copyFile(new File("examples/tile_vector_grid.png"), new File("src/main/docs/images"))
    }


}
