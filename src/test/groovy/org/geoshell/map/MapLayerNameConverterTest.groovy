package org.geoshell.map

import geoscript.layer.Layer
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.geoshell.vector.LayerCommands
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.geoshell.map.Map as GeoShellMap
import org.junit.jupiter.api.Test
import org.springframework.shell.core.Completion
import org.springframework.shell.core.MethodTarget

import java.lang.reflect.Method

import static org.junit.jupiter.api.Assertions.*

class MapLayerNameConverterTest {

    @Test
    void supports() {
        MapLayerNameConverter converter = new MapLayerNameConverter()
        assertTrue converter.supports(MapLayerName, "rivers")
        assertFalse converter.supports(LayerName, "cities")
    }

    @Test
    void convertFromText() {
        MapLayerNameConverter converter = new MapLayerNameConverter()
        assertEquals new MapLayerName("rivers"), converter.convertFromText("rivers", MapLayerName, "")
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
        GeoShellMap map1 = new GeoShellMap(new MapName("map1"), catalog)
        map1.addLayer(new MapLayerName("points"), new LayerName("points"))
        map1.addLayer(new MapLayerName("lines"), new LayerName("lines"))
        catalog.maps[new MapName("map1")] = map1

        // Create map 2
        GeoShellMap map2 = new GeoShellMap(new MapName("map2"), catalog)
        map2.addLayer(new MapLayerName("polygons"), new LayerName("polygons"))
        catalog.maps[new MapName("map2")] = map2

        MapLayerNameConverter converter = new MapLayerNameConverter(catalog: catalog)

        // No Map
        Method method = MapCommands.class.getDeclaredMethod("removeLayer", MapName, MapLayerName)
        MethodTarget target = new MethodTarget(method, new MapCommands(), "", "key")
        List<Completion> completions = []
        assertTrue converter.getAllPossibleValues(completions, MapLayerName, "", "", target)
        assertTrue new Completion("points") in completions
        assertTrue new Completion("lines") in completions
        assertTrue new Completion("polygons") in completions

        // Map1
        method = MapCommands.class.getDeclaredMethod("removeLayer", MapName, MapLayerName)
        target = new MethodTarget(method, new MapCommands(), "--name map1 --layer ", "key")
        completions = []
        assertTrue converter.getAllPossibleValues(completions, MapLayerName, "", "", target)
        assertTrue new Completion("points") in completions
        assertTrue new Completion("lines") in completions
        assertFalse new Completion("polygons") in completions

        // Map2
        method = MapCommands.class.getDeclaredMethod("removeLayer", MapName, MapLayerName)
        target = new MethodTarget(method, new MapCommands(), "--name map2 --layer ", "key")
        completions = []
        assertTrue converter.getAllPossibleValues(completions, MapLayerName, "", "", target)
        assertFalse new Completion("points") in completions
        assertFalse new Completion("lines") in completions
        assertTrue new Completion("polygons") in completions
    }
}