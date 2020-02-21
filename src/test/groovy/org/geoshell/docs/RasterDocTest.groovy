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
    void polygon() {
        run("raster_polygon", [
                "format open --name high --input src/test/resources/high.tif",
                "raster open --format high --raster high --name high",
                "workspace open --name layers --params memory",
                "raster polygon --name high --output-workspace layers --output-name grid",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/high.sld",
                "raster style set --name high --style examples/high.sld",
                "style create --params \"stroke=black stroke-width=2 label=value label-size=12\" --file examples/grid.sld",
                "layer style set --name grid --style examples/grid.sld",
                "map open --name map",
                "map add raster --name map --raster high",
                "map add layer --name map --layer grid",
                "map draw --name map --file examples/raster_polygon.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_polygon.png"), new File("src/main/docs/images"))
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

    @Test
    void addRaster() {
        run("raster_add_raster", [
                // Open high
                "format open --name high --input src/test/resources/high.tif",
                "raster open --format high --raster high --name high",

                // Create High Map
                "workspace open --name layers --params memory",
                "style create --params \"stroke=black stroke-width=2 label=value label-size=12\" --file examples/grid.sld",

                "raster polygon --name high --output-workspace layers --output-name high_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/high.sld",
                "raster style set --name high --style examples/high.sld",
                "layer style set --name high_polygons --style examples/grid.sld",
                "map open --name mapHigh",
                "map add raster --name mapHigh --raster high",
                "map add layer --name mapHigh --layer high_polygons",
                "map draw --name mapHigh --file examples/raster_add_raster_high.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapHigh",

                // Open low
                "format open --name low --input src/test/resources/low.tif",
                "raster open --format low --raster low --name low",

                // Create Low Map
                "raster polygon --name low --output-workspace layers --output-name low_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/low.sld",
                "raster style set --name low --style examples/low.sld",
                "layer style set --name low_polygons --style examples/grid.sld",
                "map open --name mapLow",
                "map add raster --name mapLow --raster low",
                "map add layer --name mapLow --layer low_polygons",
                "map draw --name mapLow --file examples/raster_add_raster_low.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapLow",

                // Add
                "format open --name add --input examples/add.tif",
                "raster add raster --name1 high --name2 low --output-format add --output-name add",

                // Create Add Map
                "raster polygon --name add --output-workspace layers --output-name add_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/add.sld",
                "raster style set --name add --style examples/add.sld",
                "layer style set --name add_polygons --style examples/grid.sld",
                "map open --name mapAdd",
                "map add raster --name mapAdd --raster add",
                "map add layer --name mapAdd --layer add_polygons",
                "map draw --name mapAdd --file examples/raster_add_raster_add.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapAdd",
        ])
        copyFile(new File("examples/raster_add_raster_high.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/raster_add_raster_low.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/raster_add_raster_add.png"), new File("src/main/docs/images"))
    }

    @Test
    void subtractRaster() {
        run("raster_subtract_raster", [
                // Open high
                "format open --name high --input src/test/resources/high.tif",
                "raster open --format high --raster high --name high",

                // Create High Map
                "workspace open --name layers --params memory",
                "style create --params \"stroke=black stroke-width=2 label=value label-size=12\" --file examples/grid.sld",

                "raster polygon --name high --output-workspace layers --output-name high_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/high.sld",
                "raster style set --name high --style examples/high.sld",
                "layer style set --name high_polygons --style examples/grid.sld",
                "map open --name mapHigh",
                "map add raster --name mapHigh --raster high",
                "map add layer --name mapHigh --layer high_polygons",
                "map draw --name mapHigh --file examples/raster_subtract_raster_high.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapHigh",

                // Open low
                "format open --name low --input src/test/resources/low.tif",
                "raster open --format low --raster low --name low",

                // Create Low Map
                "raster polygon --name low --output-workspace layers --output-name low_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/low.sld",
                "raster style set --name low --style examples/low.sld",
                "layer style set --name low_polygons --style examples/grid.sld",
                "map open --name mapLow",
                "map add raster --name mapLow --raster low",
                "map add layer --name mapLow --layer low_polygons",
                "map draw --name mapLow --file examples/raster_subtract_raster_low.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapLow",

                // Substract
                "format open --name subtract --input examples/subtract.tif",
                "raster subtract raster --name1 high --name2 low --output-format subtract --output-name subtract",

                // Create Subtract Map
                "raster polygon --name subtract --output-workspace layers --output-name subtract_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/subtract.sld",
                "raster style set --name subtract --style examples/subtract.sld",
                "layer style set --name subtract_polygons --style examples/grid.sld",
                "map open --name mapSubtract",
                "map add raster --name mapSubtract --raster subtract",
                "map add layer --name mapSubtract --layer subtract_polygons",
                "map draw --name mapSubtract --file examples/raster_subtract_raster_subtract.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapSubtract",
        ])
        copyFile(new File("examples/raster_subtract_raster_high.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/raster_subtract_raster_low.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/raster_subtract_raster_subtract.png"), new File("src/main/docs/images"))
    }

    @Test
    void multiplyRaster() {
        run("raster_multiply_raster", [
                // Open high
                "format open --name high --input src/test/resources/high.tif",
                "raster open --format high --raster high --name high",

                // Create High Map
                "workspace open --name layers --params memory",
                "style create --params \"stroke=black stroke-width=2 label=value label-size=12\" --file examples/grid.sld",

                "raster polygon --name high --output-workspace layers --output-name high_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/high.sld",
                "raster style set --name high --style examples/high.sld",
                "layer style set --name high_polygons --style examples/grid.sld",
                "map open --name mapHigh",
                "map add raster --name mapHigh --raster high",
                "map add layer --name mapHigh --layer high_polygons",
                "map draw --name mapHigh --file examples/raster_multiply_raster_high.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapHigh",

                // Open low
                "format open --name low --input src/test/resources/low.tif",
                "raster open --format low --raster low --name low",

                // Create Low Map
                "raster polygon --name low --output-workspace layers --output-name low_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/low.sld",
                "raster style set --name low --style examples/low.sld",
                "layer style set --name low_polygons --style examples/grid.sld",
                "map open --name mapLow",
                "map add raster --name mapLow --raster low",
                "map add layer --name mapLow --layer low_polygons",
                "map draw --name mapLow --file examples/raster_multiply_raster_low.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapLow",

                // Substract
                "format open --name multiply --input examples/multiply.tif",
                "raster multiply raster --name1 high --name2 low --output-format multiply --output-name multiply",

                // Create Subtract Map
                "raster polygon --name multiply --output-workspace layers --output-name multiply_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/multiply.sld",
                "raster style set --name multiply --style examples/multiply.sld",
                "layer style set --name multiply_polygons --style examples/grid.sld",
                "map open --name mapSubtract",
                "map add raster --name mapSubtract --raster multiply",
                "map add layer --name mapSubtract --layer multiply_polygons",
                "map draw --name mapSubtract --file examples/raster_multiply_raster_multiply.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapSubtract",
        ])
        copyFile(new File("examples/raster_multiply_raster_high.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/raster_multiply_raster_low.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/raster_multiply_raster_multiply.png"), new File("src/main/docs/images"))
    }

    @Test
    void divideRaster() {
        run("raster_divide_raster", [
                // Open high
                "format open --name high --input src/test/resources/high.tif",
                "raster open --format high --raster high --name high",

                // Create High Map
                "workspace open --name layers --params memory",
                "style create --params \"stroke=black stroke-width=2 label=value label-size=12\" --file examples/grid.sld",

                "raster polygon --name high --output-workspace layers --output-name high_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/high.sld",
                "raster style set --name high --style examples/high.sld",
                "layer style set --name high_polygons --style examples/grid.sld",
                "map open --name mapHigh",
                "map add raster --name mapHigh --raster high",
                "map add layer --name mapHigh --layer high_polygons",
                "map draw --name mapHigh --file examples/raster_divide_raster_high.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapHigh",

                // Open low
                "format open --name low --input src/test/resources/low.tif",
                "raster open --format low --raster low --name low",

                // Create Low Map
                "raster polygon --name low --output-workspace layers --output-name low_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/low.sld",
                "raster style set --name low --style examples/low.sld",
                "layer style set --name low_polygons --style examples/grid.sld",
                "map open --name mapLow",
                "map add raster --name mapLow --raster low",
                "map add layer --name mapLow --layer low_polygons",
                "map draw --name mapLow --file examples/raster_divide_raster_low.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapLow",

                // Substract
                "format open --name divide --input examples/divide.tif",
                "raster divide raster --name1 high --name2 low --output-format divide --output-name divide",

                // Create Subtract Map
                "raster polygon --name divide --output-workspace layers --output-name divide_polygons",
                "style raster palette colormap --min 1 --max 50 --palette MutedTerrain --number 20 --file examples/divide.sld",
                "raster style set --name divide --style examples/divide.sld",
                "layer style set --name divide_polygons --style examples/grid.sld",
                "map open --name mapSubtract",
                "map add raster --name mapSubtract --raster divide",
                "map add layer --name mapSubtract --layer divide_polygons",
                "map draw --name mapSubtract --file examples/raster_divide_raster_divide.png --bounds \"-180,-90,180,90,EPSG:4326\"",
                "map close --name mapSubtract",
        ])
        copyFile(new File("examples/raster_divide_raster_high.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/raster_divide_raster_low.png"), new File("src/main/docs/images"))
        copyFile(new File("examples/raster_divide_raster_divide.png"), new File("src/main/docs/images"))
    }

    @Test
    void reproject() {
        run("raster_reproject", [
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "format open --name earthCropped --input examples/earthCropped.tif",
                "raster crop --name earth --output-format earthCropped --output-name earthCropped --geometry \"-180.0,-85.06,180.0,85.06\"",
                "format open --name earth3857 --input examples/earth3857.tif",
                "raster reproject --name earthCropped --output-format earth3857 --output-name earth3857 --projection \"EPSG:3857\"",
                "map open --name map",
                "map add raster --name map --raster earth3857",
                "map draw --name map --file examples/raster_reproject.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_reproject.png"), new File("src/main/docs/images"))
    }

    @Test
    void crop() {
        run("raster_crop", [
                "format open --name earth --input src/test/resources/earth.tif",
                "raster open --format earth --raster earth --name earth",
                "format open --name earthCropped --input examples/earthCropped.tif",
                "raster crop --name earth --output-format earthCropped --output-name earthCropped --geometry \"-160.927734,6.751896,-34.716797,57.279043\"",
                "map open --name map",
                "map add raster --name map --raster earthCropped",
                "map draw --name map --file examples/raster_crop.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_crop.png"), new File("src/main/docs/images"))
    }

    @Test
    void contours() {
        run("raster_contours", [
                "format open --name pc --input src/test/resources/pc.tif",
                "raster open --format pc --raster pc --name pc",
                "style raster colormap --raster pc --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/pc.sld",
                "raster style set --name pc --style examples/pc.sld",
                "workspace open --name contours --params examples/contours.shp",
                "raster contours --name pc --output-workspace contours --output-name contours --levels 0,100,200,300,600,900",
                "style create --params \"stroke=black stroke-width=0.25\" --file examples/contours.sld",
                "layer style set --name contours --style examples/contours.sld",
                "map open --name map",
                "map add raster --name map --raster pc",
                "map add layer --name map --layer contours",
                "map draw --name map --file examples/raster_contours.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_contours.png"), new File("src/main/docs/images"))
    }

    @Test
    void stylize() {
        run("raster_stylize", [
                "format open --name pc --input src/test/resources/pc.tif",
                "raster open --format pc --raster pc --name pc",
                "style raster colormap --raster pc --values \"25=#9fd182,470=#3e7f3c,920=#133912,1370=#08306b,1820=#fffff5\" --file examples/pc.sld",
                "raster style set --name pc --style examples/pc.sld",
                "format open --name pcStyled --input examples/pcStyled.tif",
                "raster stylize --name pc --output-format pcStyled --output-name pcStyled",
                "map open --name map",
                "map add raster --name map --raster pcStyled",
                "map draw --name map --file examples/raster_stylize.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_stylize.png"), new File("src/main/docs/images"))
    }

    @Test
    void reclassify() {
        run("raster_reclassify", [
                "format open --name pc --input src/test/resources/pc.tif",
                "raster open --format pc --raster pc --name pc",
                "format open --name pcReclass --input examples/pcReclass.tif",
                "raster reclassify --name pc --output-format pcReclass --output-name pcReclass --ranges \"0-0=1,0-50=2,50-200=3,200-1000=4,1000-1500=5,1500-4000=6\"",
                "style raster colormap --raster pcReclass --values \"1=#9fd182,2=#3e7f3c,3=#133912,4=#08306b,5=#FFF8DC,6=#ffffff\" --file examples/pcReclass.sld",
                "raster style set --name pcReclass --style examples/pcReclass.sld",
                "map open --name map",
                "map add raster --name map --raster pcReclass",
                "map draw --name map --file examples/raster_reclassify.png",
                "map close --name map",
        ])
        copyFile(new File("examples/raster_reclassify.png"), new File("src/main/docs/images"))
    }

}
