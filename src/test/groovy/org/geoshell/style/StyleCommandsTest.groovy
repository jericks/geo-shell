package org.geoshell.style

import geoscript.workspace.Directory
import org.geoshell.Catalog
import org.geoshell.vector.LayerCommands
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.springframework.shell.support.util.OsUtils

import static org.junit.Assert.*

class StyleCommandsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

    @Test void create() {
        Catalog catalog = new Catalog()
        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File file = folder.newFile("style.sld")
        String result = cmds.create("stroke=black", file)
        assertTrue result.startsWith("Style stroke=black written to")
        assertTrue result.trim().endsWith("style.sld!")
        assertTrue file.text.contains("<sld:UserLayer>")
    }

    @Test void createDefaultVectorStyle() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("points.shp").toURI())
        catalog.workspaces[new WorkspaceName("shps")] = new Directory(file.parentFile)
        catalog.layers[new LayerName("points")] = catalog.workspaces[new WorkspaceName("shps")].get("points")

        StyleCommands cmds = new StyleCommands(catalog: catalog)
        File styleFile = folder.newFile("style.sld")
        String result = cmds.createDefaultVector(new LayerName("points"), "blue", 0.5, styleFile)
        assertTrue result.startsWith("Default Vector Style for points written to")
        assertTrue result.trim().endsWith("style.sld!")
        assertTrue styleFile.text.contains("<sld:UserLayer>")
    }
}
