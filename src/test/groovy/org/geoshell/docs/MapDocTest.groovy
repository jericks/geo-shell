package org.geoshell.docs

import org.junit.Test

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

}
