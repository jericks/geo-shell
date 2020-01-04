package org.geoshell.docs

import org.junit.Test

class StyleDocTest extends AbstractDocTest {

    @Test
    void create() {
      run("style_create", [
        "style create --params \"stroke=black stroke-width=0.25 fill=wheat\" --file examples/style_create.sld",
        "workspace open --name naturalearth --params src/test/resources/naturalearth.gpkg",
        "layer open --workspace naturalearth --layer countries --name countries",
        "layer style set --name countries --style examples/style_create.sld",
        "layer open --workspace naturalearth --layer ocean --name ocean",
        "layer style set --name ocean --style examples/ocean.sld",
        "map open --name map",
        "map add layer --name map --layer ocean",
        "map add layer --name map --layer countries",
        "map draw --name map --file examples/style_create.png",
        "map close --name map"
      ])
      copyFile(new File("examples/style_create.png"), new File("src/main/docs/images"))
      copyFile(new File("examples/style_create.sld"), new File("src/main/docs/output"))
    }

    @Test
    void vectorDefault() {

    }


    @Test
    void vectorGradient() {

    }

    @Test
    void vectorUniqueValues() {

    }

    @Test
    void vectorUniqueValuesFromTestFile() {

    }

    @Test
    void rasterDefault() {

    }

    @Test
    void rasterColorMap() {

    }

}
