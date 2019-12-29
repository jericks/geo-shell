package org.geoshell.docs

import org.junit.Test

class FormatDocTest extends AbstractDocTest {

    @Test
    void open() {
        run("format_open", [
           "format open --name earth --input src/test/resources/earth.tif",
           "format close --name earth"
        ])
    }


    @Test
    void close() {
        run("format_close", [
                "format open --name earth --input src/test/resources/earth.tif",
                "format close --name earth"
        ])
    }

    @Test
    void list() {
        run("format_list", [
                "format open --name earth --input src/test/resources/earth.tif",
                "format open --name raster --input src/test/resources/raster.tif",
                "format list",
                "format close --name earth",
                "format close --name raster"
        ])
    }

    @Test
    void rasters() {
        run("format_rasters", [
                "format open --name earth --input src/test/resources/earth.tif",
                "format rasters --name earth",
                "format close --name earth"
        ])
    }

}
