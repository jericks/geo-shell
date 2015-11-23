package org.geoshell.vector

import geoscript.layer.Layer
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.junit.Test
import org.springframework.shell.support.util.OsUtils

import static org.junit.Assert.*

class WorkspaceCommandsTest {

    @Test void open() {
        Catalog catalog = new Catalog()
        WorkspaceCommands cmds = new WorkspaceCommands(catalog: catalog)
        String result = cmds.open(new WorkspaceName("layers"), "memory")
        assertEquals "Workspace layers opened!", result
        assertNotNull catalog.workspaces[new WorkspaceName("layers")]
    }

    @Test void close() {
        Catalog catalog = new Catalog()
        WorkspaceCommands cmds = new WorkspaceCommands(catalog: catalog)
        // Open
        String result = cmds.open(new WorkspaceName("layers"), "memory")
        assertEquals "Workspace layers opened!", result
        assertNotNull catalog.workspaces[new WorkspaceName("layers")]
        // Close
        result = cmds.close(new WorkspaceName("layers"))
        assertEquals "Workspace layers closed!", result
        assertNull catalog.workspaces[new WorkspaceName("layers")]
    }

    @Test void list() {
        Catalog catalog = new Catalog()
        WorkspaceCommands cmds = new WorkspaceCommands(catalog: catalog)
        cmds.open(new WorkspaceName("shps"), "memory")
        cmds.open(new WorkspaceName("postgis"), "memory")
        cmds.open(new WorkspaceName("geopackage"), "memory")
        String result = cmds.list()
        assertEquals "shps = Memory" + OsUtils.LINE_SEPARATOR +
            "postgis = Memory" + OsUtils.LINE_SEPARATOR +
            "geopackage = Memory", result
    }

    @Test void layers() {
        Catalog catalog = new Catalog()
        Workspace workspace1 = new Memory()
        workspace1.add(new Layer("cities"))
        workspace1.add(new Layer("roads"))
        catalog.workspaces[new WorkspaceName("ws1")] = workspace1
        WorkspaceCommands cmds = new WorkspaceCommands(catalog: catalog)
        String result = cmds.layers(new WorkspaceName("ws1"))
        assertEquals "cities" + OsUtils.LINE_SEPARATOR + "roads", result
    }

}
