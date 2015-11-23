package org.geoshell.vector

import geoscript.workspace.Memory
import org.geoshell.Catalog
import org.junit.Test
import org.springframework.shell.core.Completion
import static org.junit.Assert.*

class WorkspaceNameConverterTest {

    @Test
    void supports() {
        WorkspaceNameConverter converter = new WorkspaceNameConverter()
        assertTrue converter.supports(WorkspaceName, "shps")
        assertFalse converter.supports(LayerName, "cities")
    }

    @Test
    void convertFromText() {
        WorkspaceNameConverter converter = new WorkspaceNameConverter()
        assertEquals new WorkspaceName("shps"), converter.convertFromText("shps", WorkspaceName, "")
    }

    @Test
    void getAllPossibleValues() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("shps")] = new Memory()
        catalog.workspaces[new WorkspaceName("postgis")] = new Memory()

        WorkspaceNameConverter converter = new WorkspaceNameConverter(catalog: catalog)
        List<Completion> completions = []
        assertTrue converter.getAllPossibleValues(completions, WorkspaceName, "", "", null)
        assertTrue new Completion("shps") in completions
        assertTrue new Completion("postgis") in completions
    }

}
