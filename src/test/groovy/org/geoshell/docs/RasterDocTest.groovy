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
                "map draw --name map --file examples/raster_style_get.png",
                "map close --name map",
                "raster style get --name pc --style examples/pc_style.sld"
        ])
        copyFile(new File("examples/raster_style_get.png"), new File("src/main/docs/images"))
    }

    @Test
    void addConstant() {
        run("raster_add_constant", [
                "format open --name pierce_county --input src/test/resources/pc.tif",
                "raster open --format pierce_county --raster pc --name pc",
                "raster value --name pc --x -121.799927 --y 46.867703",
                "format open --name pcAdd100 --input examples/pcAdd100.tif",
                "raster add constant --name pc --output-format pcAdd100 --output-name pcAdd100 --values 100",
                "raster value --name pcAdd100 --x -121.799927 --y 46.867703",
                "style raster colormap --raster pcAdd100 --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/style_raster_colormap.sld",
                "raster style set --name pcAdd100 --style examples/style_raster_colormap.sld",
                "map open --name map",
                "map add raster --name map --raster pcAdd100",
                "map draw --name map --file examples/raster_add_constant.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_add_constant.png"), new File("src/main/docs/images"))
    }

    @Test
    void subtractConstant() {
        run("raster_subtract_constant", [
                "format open --name pierce_county --input src/test/resources/pc.tif",
                "raster open --format pierce_county --raster pc --name pc",
                "raster value --name pc --x -121.799927 --y 46.867703",
                "format open --name pcMinus100 --input examples/pcMinus100.tif",
                "raster subtract constant --name pc --output-format pcMinus100 --output-name pcMinus100 --values 100",
                "raster value --name pcMinus100 --x -121.799927 --y 46.867703",
                "style raster colormap --raster pcMinus100 --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/style_raster_colormap.sld",
                "raster style set --name pcMinus100 --style examples/style_raster_colormap.sld",
                "map open --name map",
                "map add raster --name map --raster pcMinus100",
                "map draw --name map --file examples/raster_subtract_constant.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_subtract_constant.png"), new File("src/main/docs/images"))
    }

    @Test
    void multiplyConstant() {
        run("raster_multiply_constant", [
                "format open --name pierce_county --input src/test/resources/pc.tif",
                "raster open --format pierce_county --raster pc --name pc",
                "raster value --name pc --x -121.799927 --y 46.867703",
                "format open --name pcTimes2 --input examples/pcTimes2.tif",
                "raster multiply constant --name pc --output-format pcTimes2 --output-name pcTimes2 --values 2",
                "raster value --name pcTimes2 --x -121.799927 --y 46.867703",
                "style raster colormap --raster pcTimes2 --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/style_raster_colormap.sld",
                "raster style set --name pcTimes2 --style examples/style_raster_colormap.sld",
                "map open --name map",
                "map add raster --name map --raster pcTimes2",
                "map draw --name map --file examples/raster_multiply_constant.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_multiply_constant.png"), new File("src/main/docs/images"))
    }

    @Test
    void divideConstant() {
        run("raster_divide_constant", [
                "format open --name pierce_county --input src/test/resources/pc.tif",
                "raster open --format pierce_county --raster pc --name pc",
                "raster value --name pc --x -121.799927 --y 46.867703",
                "format open --name pcDividedBy2 --input examples/pcDividedBy2.tif",
                "raster divide constant --name pc --output-format pcDividedBy2 --output-name pcDividedBy2 --values 2",
                "raster value --name pcDividedBy2 --x -121.799927 --y 46.867703",
                "style raster colormap --raster pcDividedBy2 --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/style_raster_colormap.sld",
                "raster style set --name pcDividedBy2 --style examples/style_raster_colormap.sld",
                "map open --name map",
                "map add raster --name map --raster pcDividedBy2",
                "map draw --name map --file examples/raster_divide_constant.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_divide_constant.png"), new File("src/main/docs/images"))
    }

}
