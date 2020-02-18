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
        run("style_vector_default", [
                "workspace open --name naturalearth --params src/test/resources/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "style vector default --layer countries --color #F5F5DC --file examples/countries_default.sld",
                "layer style set --name countries --style examples/countries_default.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "style vector default --layer ocean --color DeepSkyBlue --file examples/ocean_default.sld",
                "layer style set --name ocean --style examples/ocean_default.sld",
                "map open --name map",
                "map add layer --name map --layer ocean",
                "map add layer --name map --layer countries",
                "map draw --name map --file examples/style_vector_default.png",
                "map close --name map"
        ])
        copyFile(new File("examples/style_vector_default.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/countries_default.sld"), new File("src/main/docs/output"))
        copyFile(new File("examples/ocean_default.sld"), new File("src/main/docs/output"))

    }


    @Test
    void vectorGradient() {
        run("style_vector_gradient", [
                "workspace open --name naturalearth --params src/test/resources/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "style vector gradient --layer countries --field PEOPLE --colors greens --number 8 --method quantile --file examples/style_vector_gradient.sld",
                "layer style set --name countries --style examples/style_vector_gradient.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name map",
                "map add layer --name map --layer ocean",
                "map add layer --name map --layer countries",
                "map draw --name map --file examples/style_vector_gradient.png",
                "map close --name map"
        ])
        copyFile(new File("examples/style_vector_gradient.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/style_vector_gradient.sld"), new File("src/main/docs/output"))
    }

    @Test
    void vectorUniqueValues() {
        run("style_vector_uniquevalues", [
                "workspace open --name naturalearth --params src/test/resources/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "style vector uniquevalues --layer countries --field NAME --colors random --file examples/style_vector_uniquevalues.sld",
                "layer style set --name countries --style examples/style_vector_uniquevalues.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name map",
                "map add layer --name map --layer ocean",
                "map add layer --name map --layer countries",
                "map draw --name map --file examples/style_vector_uniquevalues.png",
                "map close --name map"
        ])
        copyFile(new File("examples/style_vector_uniquevalues.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/style_vector_uniquevalues.sld"), new File("src/main/docs/output"))
    }

    @Test
    void vectorUniqueValuesFromTextFile() {
        run("style_vector_uniquevaluesfromtext", [
                "workspace open --name mars --params src/test/resources/mars",
                "layer open --workspace mars --layer geo_units_oc_dd --name mars",
                "style vector uniquevaluesfromtext --field UnitSymbol --textFile src/test/resources/mars/I1802ABC_geo_units_RGBlut.txt --geometryType polygon --styleFile examples/style_vector_uniquevaluesfromtext.sld",
                "layer style set --name mars --style examples/style_vector_uniquevaluesfromtext.sld",
                "map open --name map",
                "map add layer --name map --layer mars",
                "map draw --name map --file examples/style_vector_uniquevaluesfromtext.png",
                "map close --name map"
        ])
        copyFile(new File("examples/style_vector_uniquevaluesfromtext.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/style_vector_uniquevaluesfromtext.sld"), new File("src/main/docs/output"))
    }

    @Test
    void rasterDefault() {
        run("style_raster_default", [
                "format open --name pierce_county --input src/test/resources/pc.tif",
                "raster open --format pierce_county --raster pc --name pc",
                "style raster default --raster pc --opacity 0.75 --file examples/style_raster_default.sld",
                "raster style set --name pc --style examples/style_raster_default.sld",
                "map open --name map",
                "map add raster --name map --raster pc",
                "map draw --name map --file examples/style_raster_default.png",
                "map close --name map"
        ])
        copyFile(new File("examples/style_raster_default.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/style_raster_default.sld"), new File("src/main/docs/output"))
    }

    @Test
    void rasterColorMap() {
        run("style_raster_colormap", [
                "format open --name pierce_county --input src/test/resources/pc.tif",
                "raster open --format pierce_county --raster pc --name pc",
                "style raster colormap --raster pc --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/style_raster_colormap.sld",
                "raster style set --name pc --style examples/style_raster_colormap.sld",
                "map open --name map",
                "map add raster --name map --raster pc",
                "map draw --name map --file examples/style_raster_colormap.png",
                "map close --name map"
        ])
        copyFile(new File("examples/style_raster_colormap.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/style_raster_colormap.sld"), new File("src/main/docs/output"))
    }

    @Test
    void polygon() {
        run("style_raster_palette_colormap", [
                "format open --name high --input src/test/resources/high.tif",
                "raster open --format high --raster high --name high",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/style_raster_palette_colormap.sld",
                "raster style set --name high --style examples/style_raster_palette_colormap.sld",
                "map open --name map",
                "map add raster --name map --raster high",
                "map draw --name map --file examples/style_raster_palette_colormap.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name map",
        ])
        copyFile(new File("examples/style_raster_palette_colormap.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/style_raster_palette_colormap.sld"), new File("src/main/docs/output"))
    }

}
