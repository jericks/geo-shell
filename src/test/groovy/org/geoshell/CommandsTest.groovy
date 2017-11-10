package org.geoshell

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import static org.junit.Assert.*

class CommandsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test void download() {
        URL url = getClass().getClassLoader().getResource("points.zip")
        File file = temporaryFolder.newFile("points.zip")
        Commands cmds = new Commands()
        cmds.download(url.toString(), file)
        assertTrue file.exists()
        assertTrue file.length() > 100
    }

    @Test void unzip() {
        File file = new File(getClass().getClassLoader().getResource("points.zip").toURI())
        File directory = temporaryFolder.newFolder("pointszip")
        Commands cmds = new Commands()
        cmds.unzip(file, directory)
        List<File> files = directory.listFiles()
        assertFalse files.isEmpty()
        ["points.shp", "points.dbf", "points.prj"].each { String name ->
            assertNotNull files.find { File f -> f.name.equals(name) }
        }
    }
}
