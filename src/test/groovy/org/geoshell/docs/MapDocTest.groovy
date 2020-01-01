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



}
