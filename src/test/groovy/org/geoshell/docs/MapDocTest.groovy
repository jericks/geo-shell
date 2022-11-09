package org.geoshell.docs

import org.junit.jupiter.api.Test

class MapDocTest extends AbstractDocTest {

    @Test
    void open() {
        run("map_open", [
                "map open --name earth",
                "map close --name earth"
        ])
    }


    @Test
    void close() {
        run("map_close", [
                "map open --name earth",
                "map close --name earth"
        ])
    }

    @Test
    void list() {
        run("map_list", [
                "map open --name earth",
                "map open --name us",
                "map list",
                "map close --name earth",
                "map close --name us"
        ])
    }

    @Test
    void addLayer() {
        run("map_add_layer", [
                "map open --name world",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map add layer --name world --layer ocean",
                "map add layer --name world --layer countries",
                "map draw --name world --file examples/map_add_layer.png",
                "map close --name world"
        ])
        copyFile(new File("examples/map_add_layer.png"), new File("src/main/docs/images"))
    }

    @Test
    void removeLayer() {
        run("map_remove_layer", [
                "map open --name world",
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "map add raster --name world --raster earth",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "style create --params \"stroke=black stroke-width=0.1\" --file examples/outline.sld",
                "layer style set --name countries --style examples/outline.sld",
                "map add layer --name world --layer countries",
                "map layers --name world",
                "map remove layer --name world --layer countries",
                "map layers --name world",
                "map close --name world"
        ])
    }

    @Test
    void reorder() {
        run("map_reorder", [
                "map open --name world",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "style create --params \"stroke=black stroke-width=0.1\" --file examples/outline.sld",
                "layer style set --name countries --style examples/outline.sld",
                "map add layer --name world --layer countries",
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "map add raster --name world --raster earth",
                "map layers --name world",
                "map reorder --name world --layer countries --order 1",
                "map layers --name world",
                "map draw --name world --file examples/map_reorder.png",
                "map close --name world"
        ])
        copyFile(new File("examples/map_reorder.png"), new File("src/main/docs/images"))
    }

    @Test
    void addRaster() {
        run("map_add_raster", [
                "map open --name world",
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "map add raster --name world --raster earth",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "style create --params \"stroke=black stroke-width=0.1\" --file examples/outline.sld",
                "layer style set --name countries --style examples/outline.sld",
                "map add layer --name world --layer countries",
                "map draw --name world --file examples/map_add_raster.png",
                "map close --name world"
        ])
        copyFile(new File("examples/map_add_raster.png"), new File("src/main/docs/images"))
    }

    @Test
    void addTileLayer() {
        run("map_add_tile", [
                "map open --name world",
                "tile open --name tiles --params src/test/resources/countries.mbtiles",
                "map add tile --name world --tile tiles",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer places --name places",
                "style vector default --layer places --color #1E90FF --file examples/places.sld",
                "layer style set --name places --style examples/places.sld",
                "map add layer --name world --layer places",
                "map draw --name world --width 400 --height 400 --file examples/map_add_tile.png",
                "map close --name world"
        ])
        copyFile(new File("examples/map_add_tile.png"), new File("src/main/docs/images"))
    }

    @Test
    void layers() {
        run("map_layers", [
                "map open --name world",
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "map add raster --name world --raster earth",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "style create --params \"stroke=black stroke-width=0.1\" --file examples/outline.sld",
                "layer style set --name countries --style examples/outline.sld",
                "map add layer --name world --layer countries",
                "map layers --name world",
                "map close --name world"
        ])
    }

    @Test
    void draw() {
        run("map_draw", [
                "map open --name world",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map add layer --name world --layer ocean",
                "map add layer --name world --layer countries",
                "map draw --name world --file examples/map_draw.png",
                "map close --name world"
        ])
        copyFile(new File("examples/map_draw.png"), new File("src/main/docs/images"))
    }

    @Test
    void mapCube() {
        run("map_cube", [
                "map open --name world",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map add layer --name world --layer ocean",
                "map add layer --name world --layer countries",
                "map cube --name world --file examples/map_cube.png --title World --source NaturalEarth --draw-tabs true --draw-outline true",
                "map close --name world"
        ])
        copyFile(new File("examples/map_cube.png"), new File("src/main/docs/images"))
    }

}
