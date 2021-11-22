package org.geoshell.docs

import org.junit.jupiter.api.Test

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

    @Test
    void generate() {
        run("tile_generate", [
                "tile open --name tiles --params target/tiles.mbtiles",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name world",
                "map add layer --name world --layer ocean",
                "map add layer --name world --layer countries",
                "tile generate --name tiles --map world --start 0 --end 3",
                "format open --name world_level2 --input examples/tile_generate.png",
                "tile stitch raster --name tiles --format world_level2 --raster world_level2 --z 2",
                "map close --name world"
        ])
        copyFile(new File("examples/tile_generate.png"), new File("src/main/docs/images"))
    }

    @Test
    void delete() {
        run("tile_delete", [
                "tile open --name tiles --params target/tiles.mbtiles",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name world",
                "map add layer --name world --layer ocean",
                "map add layer --name world --layer countries",
                "tile generate --name tiles --map world --start 0 --end 3",
                "tile delete --name tiles --z 3",
                "map close --name world"
        ])
    }

    @Test
    void tiles() {
        run("tile_tiles", [
                "tile open --name countries --params src/test/resources/countries.mbtiles",
                "tile tiles --name countries --z 8 --bounds -13787405.4140,5872198.2610,-13349574.1159,6081635.7185",
                "tile close --name countries"
        ])
    }

    @Test
    void stitch() {
        run("tile_stitch_bounds", [
                "tile open --name countries --params src/test/resources/countries.mbtiles",
                "format open --name states --input examples/tile_stitch_bounds.png",
                "tile stitch raster --name countries --format states --raster states --bounds -18217695.5734,1222992.4526,-4207094.0368,7924991.0926"
        ])
        copyFile(new File("examples/tile_stitch_bounds.png"), new File("src/main/docs/images"))
    }

}
