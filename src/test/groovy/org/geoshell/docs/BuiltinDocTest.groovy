package org.geoshell.docs

import org.geotools.data.ogr.OGRDataStoreFactory
import org.geotools.util.logging.Logging
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import java.util.logging.Level
import java.util.logging.Logger

class BuiltinDocTest extends AbstractDocTest {

    @Test
    void exit() {
        run("exit", [
                "exit"
        ], [captureOutput: true])
    }

    @Test
    void date() {
        run("date", [
                "date"
        ], [captureOutput: true])
    }

    @Test
    void help() {
        run("help", [
                "help"
        ], [captureOutput: true])
    }

    @Test
    void helpCommand() {
        run("help_command", [
                "help layer open"
        ], [captureOutput: true])
    }

    @Test
    void version() {
        run("version", [
                "version"
        ], [captureOutput: true])
    }

    @Test
    void systemProperties() {
        run("system_properties", [
                "system properties"
        ], [captureOutput: true])
    }

    @Test
    void runOs() {
        run("run", [
                "! ls src/test/resources/mars"
        ], [captureOutput: true])
    }



    @Test
    void script() {
        run("script", [
                "script src/test/resources/layer_count.txt"
        ], [captureOutput: true])
        copyFile(new File("src/test/resources/layer_count.txt"), new File("src/main/docs/output"))
    }

    @Disabled
    @Test
    void download() {
        run("download", [
                "download --url https://astropedia.astrogeology.usgs.gov/download/Mars/Geology/Mars15MGeologicGISRenovation.zip --file mars.zip --overwrite false",
                "unzip --file mars.zip --directory mars",
                "style vector uniquevaluesfromtext --field UnitSymbol --geometryType Polygon --styleFile mars/units.sld --textFile mars/I1802ABC_Mars_global_geology/I1802ABC_geo_units_RGBlut.txt",
                "workspace open --name mars --params mars/I1802ABC_Mars_global_geology/Shapefiles/I1802ABC_Mars2000_Sphere/geo_units_oc_dd.shp",
                "layer open --workspace mars --layer geo_units_oc_dd",
                "layer style set --name mars:geo_units_oc_dd --style mars/units.sld",
                "map open --name mars",
                "map add layer --name mars --layer mars:geo_units_oc_dd",
                "map draw --name mars",
                "map close --name mars",
                "open --file image.png"

        ])
    }

}
