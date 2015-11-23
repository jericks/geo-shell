package org.geoshell.vector

import geoscript.layer.Layer
import geoscript.layer.Shapefile
import geoscript.workspace.Directory
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.junit.Test
import org.springframework.shell.support.util.OsUtils
import static org.junit.Assert.*

class LayerCommandsTest {

    @Test void open() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        workspace.add(new Layer("lines"))
        workspace.add(new Layer("polygons"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace

        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.open(new WorkspaceName("shapes"), new LayerName("points"), null)
        assertEquals "Opened Workspace shapes Layer points as shapes:points", result
        assertNotNull catalog.layers[new LayerName("shapes:points")]

        result = cmds.open(new WorkspaceName("shapes"), new LayerName("lines"), "lines")
        assertEquals "Opened Workspace shapes Layer lines as lines", result
        assertNotNull catalog.layers[new LayerName("lines")]
    }

    @Test void close() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        workspace.add(new Layer("lines"))
        workspace.add(new Layer("polygons"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace

        LayerCommands cmds = new LayerCommands(catalog: catalog)
        // Open
        cmds.open(new WorkspaceName("shapes"), new LayerName("points"), "")
        assertNotNull catalog.layers[new LayerName("shapes:points")]
        // Close
        String result = cmds.close(new LayerName("shapes:points"))
        assertEquals "Layer shapes:points closed!", result
        assertNull catalog.layers[new LayerName("shapes:points")]
    }

    @Test void list() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        workspace.add(new Layer("lines"))
        workspace.add(new Layer("polygons"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace

        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.open(new WorkspaceName("shapes"), new LayerName("points"), null)
        cmds.open(new WorkspaceName("shapes"), new LayerName("lines"), "lines")
        String result = cmds.list()
        assertEquals "shapes:points = Memory" + OsUtils.LINE_SEPARATOR + "lines = Memory", result
    }


    @Test void count() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.count(new LayerName("points"))
        assertEquals "10", result
    }

    @Test
    void schema() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("points.shp").toURI())
        catalog.workspaces[new WorkspaceName("shps")] = new Directory(file.parentFile)
        catalog.layers[new LayerName("points")] = catalog.workspaces[new WorkspaceName("shps")].get("points")

        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String actual = cmds.schema(new LayerName("points"))
        String expected = "  Name                  Type" + OsUtils.LINE_SEPARATOR +
                "  --------------------  --------------------" + OsUtils.LINE_SEPARATOR +
                "  the_geom              Point" + OsUtils.LINE_SEPARATOR +
                "  id                    Integer" + OsUtils.LINE_SEPARATOR
        assertEquals expected, actual
    }

    @Test void buffer() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.layers[new LayerName("points")] = layer
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)

        String result = cmds.buffer(new LayerName("points"), new WorkspaceName("mem"), "polys", 10)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("polys")]
        Layer bufferedLayer = catalog.layers[new LayerName("polys")]
        assertEquals layer.count, bufferedLayer.count
        assertEquals "Polygon", bufferedLayer.schema.geom.typ
    }

    @Test void centroid() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.layers[new LayerName("grid")] = layer
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)

        String result = cmds.centroids(new LayerName("grid"), new WorkspaceName("mem"), "centroids")
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("centroids")]
        Layer centroidLayer = catalog.layers[new LayerName("centroids")]
        assertEquals layer.count, centroidLayer.count
        assertEquals "Point", centroidLayer.schema.geom.typ
    }

    @Test void random() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)

        String result = cmds.random(new WorkspaceName("mem"), "points", 100, "0,0,100,100", "EPSG:4326", "id", "geom", false, false, 0)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("points")]
        Layer layer = catalog.layers[new LayerName("points")]
        assertEquals 100, layer.count
        assertEquals "Point", layer.schema.geom.typ
    }
}
