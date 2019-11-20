package org.geoshell.docs

import org.junit.Test

class LayerDocTest extends AbstractDocTest {

    @Test
    void createRandomPoints() {
        run("layer_random", [
                "workspace open --name layers --params memory",
                "layer random --output-workspace layers --output-name points --geometry -180,-90,180,90 --number 100 --projection EPSG:4326",
                "style vector default --layer points --color #1E90FF --file examples/points.sld",
                "layer style set --name points --style examples/points.sld",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name randomMap",
                "map add layer --name randomMap --layer ocean",
                "map add layer --name randomMap --layer countries",
                "map add layer --name randomMap --layer points",
                "map draw --name randomMap --file examples/random_points.png",
                "map close --name randomMap"
        ])
        copyFile(new File("examples/random_points.png"), new File("src/main/docs/images"))
    }

    @Test
    void createSquareGraticules() {
        run("layer_graticule_square", [
            "workspace open --name layers --params memory",
            "layer graticule square --workspace layers --name squares --bounds -180,-90,180,90 --length 20",
            "style vector default --layer squares --color #1E90FF --opacity 0.30 --file examples/squares.sld",
            "layer style set --name squares --style examples/squares.sld",
            "workspace open --name naturalearth --params examples/naturalearth.gpkg",
            "layer open --workspace naturalearth --layer countries --name countries",
            "layer style set --name countries --style examples/countries.sld",
            "layer open --workspace naturalearth --layer ocean --name ocean",
            "layer style set --name ocean --style examples/ocean.sld",
            "map open --name graticule",
            "map add layer --name graticule --layer ocean",
            "map add layer --name graticule --layer countries",
            "map add layer --name graticule --layer squares",
            "map draw --name graticule --file examples/square_graticules.png",
            "map close --name graticule"
        ])
        copyFile(new File("examples/square_graticules.png"), new File("src/main/docs/images"))
    }

}
