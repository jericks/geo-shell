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

}
