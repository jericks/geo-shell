package org.geoshell.docs

import org.junit.Test

class WorkspaceDocTest extends AbstractDocTest {

    @Test
    void workspaceOpenListClose() {
        run([
                "workspace_basics_open":  "workspace open --name mem --params memory",
                "workspace_basics_list":  "workspace list",
                "workspace_basics_close": "workspace close --name mem"
        ])
    }

    @Test
    void workspaceLayers() {
        run([
                "workspace_layers_open":   "workspace open --name naturalearth --params src/test/resources/naturalearth.gpkg",
                "workspace_layers_layers": "workspace layers --name naturalearth",
                "workspace_layers_close":  "workspace close --name naturalearth"
        ])
    }

}
