package org.geoshell.style

import org.geoshell.Catalog
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
}
