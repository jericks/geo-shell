package org.geoshell.map

import geoscript.layer.Layer
import geoscript.layer.Shapefile
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.shell.support.util.OsUtils

import static org.junit.jupiter.api.Assertions.*

class MapCommandsTest {

    @TempDir
    File folder

    @Test void open() {
        Catalog catalog = new Catalog()
        MapCommands cmds = new MapCommands(catalog: catalog)
        String result = cmds.open(new MapName("washington"))
        assertEquals "Map washington opened!", result
    }

    @Test void close() {
        Catalog catalog = new Catalog()
        MapCommands cmds = new MapCommands(catalog: catalog)
        cmds.open(new MapName("washington"))
        String result = cmds.close(new MapName("washington"))
        assertEquals "Map washington closed!", result
        result = cmds.close(new MapName("washington"))
        assertEquals "Unable to find Map washington", result
    }

    @Test void list() {
        Catalog catalog = new Catalog()
        MapCommands cmds = new MapCommands(catalog: catalog)
        cmds.open(new MapName("washington"))
        cmds.open(new MapName("oregon"))
        String result = cmds.list()
        assertEquals "washington" + OsUtils.LINE_SEPARATOR + "oregon", result
    }

    @Test void layers() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        workspace.add(new Layer("lines"))
        workspace.add(new Layer("polygons"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace
        catalog.layers[new LayerName("points")] = workspace["points"]
        catalog.layers[new LayerName("lines")] = workspace["lines"]
        catalog.layers[new LayerName("polygons")] = workspace["polygons"]

        MapCommands cmds = new MapCommands(catalog: catalog)
        cmds.open(new MapName("washington"))
        cmds.addLayer(new MapName("washington"), new LayerName("points"), null)
        cmds.addLayer(new MapName("washington"), new LayerName("lines"), null)
        cmds.addLayer(new MapName("washington"), new LayerName("polygons"), null)
        String result = cmds.info(new MapName("washington"))
        assertEquals "points" + OsUtils.LINE_SEPARATOR + "lines" + OsUtils.LINE_SEPARATOR + "polygons" + OsUtils.LINE_SEPARATOR, result
    }

    @Test void addLayer() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace
        catalog.layers[new LayerName("points")] = workspace["points"]

        MapCommands cmds = new MapCommands(catalog: catalog)
        cmds.open(new MapName("washington"))
        String result = cmds.addLayer(new MapName("washington"), new LayerName("points"), null)
        assertEquals "Added points layer to map washington", result
    }

    @Test void removeLayer() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace
        catalog.layers[new LayerName("points")] = workspace["points"]

        MapCommands cmds = new MapCommands(catalog: catalog)
        cmds.open(new MapName("washington"))
        cmds.addLayer(new MapName("washington"), new LayerName("points"), null)
        String result = cmds.removeLayer(new MapName("washington"), new MapLayerName("points"))
        assertEquals "Removed points layer from map washington", result
    }

    @Test void reorder() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        workspace.add(new Layer("lines"))
        workspace.add(new Layer("polygons"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace
        catalog.layers[new LayerName("points")] = workspace["points"]
        catalog.layers[new LayerName("lines")] = workspace["lines"]
        catalog.layers[new LayerName("polygons")] = workspace["polygons"]

        MapCommands cmds = new MapCommands(catalog: catalog)
        cmds.open(new MapName("washington"))
        cmds.addLayer(new MapName("washington"), new LayerName("points"), null)
        cmds.addLayer(new MapName("washington"), new LayerName("lines"), null)
        cmds.addLayer(new MapName("washington"), new LayerName("polygons"), null)
        String result = cmds.info(new MapName("washington"))
        assertEquals "points" + OsUtils.LINE_SEPARATOR + "lines" + OsUtils.LINE_SEPARATOR + "polygons" + OsUtils.LINE_SEPARATOR, result
        // Absolute
        result = cmds.reorder(new MapName("washington"), new MapLayerName("lines"), "0")
        assertEquals "Moved lines from 1 to 0", result
        result = cmds.info(new MapName("washington"))
        assertEquals "lines" + OsUtils.LINE_SEPARATOR + "points" + OsUtils.LINE_SEPARATOR + "polygons" + OsUtils.LINE_SEPARATOR, result
        // Relative
        result = cmds.reorder(new MapName("washington"), new MapLayerName("lines"), "-1")
        assertEquals "Moved lines from 0 to 1", result
        result = cmds.info(new MapName("washington"))
        assertEquals "points" + OsUtils.LINE_SEPARATOR + "lines" + OsUtils.LINE_SEPARATOR + "polygons" + OsUtils.LINE_SEPARATOR, result
        // up
        result = cmds.reorder(new MapName("washington"), new MapLayerName("polygons"), "up")
        assertEquals "Moved polygons from 2 to 1", result
        result = cmds.info(new MapName("washington"))
        assertEquals "points" + OsUtils.LINE_SEPARATOR + "polygons" + OsUtils.LINE_SEPARATOR + "lines" + OsUtils.LINE_SEPARATOR, result
    }

    @Test void draw() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.layers[new LayerName("grid")] = layer

        MapCommands cmds = new MapCommands(catalog: catalog)
        cmds.open(new MapName("grid"))
        cmds.addLayer(new MapName("grid"), new LayerName("grid"), null)
        File file = new File(folder, "map.png")
        String result = cmds.render(new MapName("grid"), null, null, 400, 300, "png", file, null)
        assertTrue result.startsWith("Done drawing")
        assertTrue result.endsWith("map.png!")
    }

    @Test void drawMapCube() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.layers[new LayerName("grid")] = layer

        MapCommands cmds = new MapCommands(catalog: catalog)
        cmds.open(new MapName("grid"))
        cmds.addLayer(new MapName("grid"), new LayerName("grid"), null)
        File file = new File(folder, "mapcube.png")
        String result = cmds.renderMapCube(
                new MapName("grid"),
                true,
                true,
                30,
                "World Grid",
                "Natural Earth",
                "png",
                file
        )
        assertTrue result.startsWith("Done drawing")
        assertTrue result.endsWith("mapcube.png!")
    }

}
