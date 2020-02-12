package org.geoshell.docs

import org.junit.Test

class RasterDocTest extends AbstractDocTest {

    @Test
    void open() {
        run("raster_open", [
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "raster close --name earth",
                "format close --name earth"
        ])
    }

    @Test
    void close() {
        run("raster_close", [
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "raster close --name earth",
                "format close --name earth"
        ])
    }

    @Test
    void list() {
        run("raster_list", [
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "raster list",
                "raster close --name earth",
                "format close --name earth"
        ])
    }

    @Test
    void info() {
        run("raster_info", [
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "raster info --name earth",
                "raster close --name earth",
                "format close --name earth"
        ])
    }

    @Test
    void value() {
        run("raster_value", [
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "raster value --name earth --x 60 --y 45",
                "raster value --name earth --x 10 --y 15 --type pixel",
                "raster close --name earth",
                "format close --name earth"
        ])
    }

    @Test
    void envelope() {
        run("raster_envelope", [
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "workspace open --name layers --params memory",
                "raster envelope --name earth --output-workspace layers --output-name outline",
                "style create --params \"stroke=black stroke-width=3\" --file examples/outline.sld",
                "layer style set --name outline --style examples/outline.sld",
                "map open --name map",
                "map add raster --name map --raster earth",
                "map add layer --name map --layer outline",
                "map draw --name map --file examples/raster_envelope.png",
                "map close --name map"
        ])
        copyFile(new File("examples/raster_envelope.png"), new File("src/main/docs/images"))
    }

    @Test
    void setStyle() {
        run("raster_style_set", [
                "format open --name pierce_county --input src/test/resources/pc.tif",
                "raster open --format pierce_county --raster pc --name pc",
                "style raster colormap --raster pc --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/style_raster_colormap.sld",
                "raster style set --name pc --style examples/style_raster_colormap.sld",
                "map open --name map",
                "map add raster --name map --raster pc",
                "map draw --name map --file examples/raster_style_set.png",
                "map close --name map"
        ])
        copyFile(new File("examples/raster_style_set.png"), new File("src/main/docs/images"))
    }

    @Test
    void getStyle() {
        run("raster_style_get", [
                "format open --name pierce_county --input src/test/resources/pc.tif",
                "raster open --format pierce_county --raster pc --name pc",
                "style raster colormap --raster pc --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/style_raster_colormap.sld",
                "raster style set --name pc --style examples/style_raster_colormap.sld",
                "map open --name map",
                "map add raster --name map --raster pc",
                "map draw --name map --file examples/raster_style_set.png",
                "map close --name map",
                "raster style get --name pc --style examples/pc_style.sld"
        ])
        copyFile(new File("examples/raster_style_set.png"), new File("src/main/docs/images"))
    }

}
