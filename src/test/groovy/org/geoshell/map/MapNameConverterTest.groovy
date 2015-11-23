package org.geoshell.map

import geoscript.layer.Layer
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.junit.Test
import org.springframework.shell.core.Completion
import org.springframework.shell.core.MethodTarget

import java.lang.reflect.Method

import static org.junit.Assert.*

class MapNameConverterTest {

    @Test
    void supports() {
        MapNameConverter converter = new MapNameConverter()
        assertTrue converter.supports(MapName, "map1")
        assertFalse converter.supports(LayerName, "cities")
    }

    @Test
    void convertFromText() {
        MapNameConverter converter = new MapNameConverter()
        assertEquals new MapName("washington"), converter.convertFromText("washington", MapName, "")
    }

    @Test
    void getAllPossibleValues() {

        // Create a new Catalog
        Catalog catalog = new Catalog()

        // Set up a memory workspace with 3 layers
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        workspace.add(new Layer("lines"))
        workspace.add(new Layer("polygons"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace
        catalog.layers[new LayerName("points")] = workspace["points"]
        catalog.layers[new LayerName("lines")] = workspace["lines"]
        catalog.layers[new LayerName("polygons")] = workspace["polygons"]

        // Create map 1
        Map map1 = new Map(new MapName("map1"), catalog)
        map1.addLayer(new MapLayerName("points"), new LayerName("points"))
        map1.addLayer(new MapLayerName("lines"), new LayerName("lines"))
        catalog.maps[new MapName("map1")] = map1

        // Create map 2
        Map map2 = new Map(new MapName("map2"), catalog)
        map2.addLayer(new MapLayerName("polygons"), new LayerName("polygons"))
        catalog.maps[new MapName("map2")] = map2

        MapNameConverter converter = new MapNameConverter(catalog: catalog)

        // No Map
        List<Completion> completions = []
        assertTrue converter.getAllPossibleValues(completions, MapName, "", "", null)
        assertTrue new Completion("map1") in completions
        assertTrue new Completion("map2") in completions
        assertFalse new Completion("points") in completions
    }

}
