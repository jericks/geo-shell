package org.geoshell

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import static org.junit.jupiter.api.Assertions.*

class CommandsTest {

    @TempDir
    File folder

    @Test void download() {
        URL url = getClass().getClassLoader().getResource("points.zip")
        File file = new File(folder, "points.zip")
        Commands cmds = new Commands()
        cmds.download(url.toString(), file, true)
        assertTrue file.exists()
        assertTrue file.length() > 100
    }

    @Test void unzip() {
        File file = new File(getClass().getClassLoader().getResource("points.zip").toURI())
        File directory = new File(folder, "pointszip")
        Commands cmds = new Commands()
        cmds.unzip(file, directory)
        List<File> files = directory.listFiles()
        assertFalse files.isEmpty()
        ["points.shp", "points.dbf", "points.prj"].each { String name ->
            assertNotNull files.find { File f -> f.name.equals(name) }
        }
    }
}
