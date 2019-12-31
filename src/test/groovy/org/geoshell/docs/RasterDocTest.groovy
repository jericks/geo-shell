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

}
