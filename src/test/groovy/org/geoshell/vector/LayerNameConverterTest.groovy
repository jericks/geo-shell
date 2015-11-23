package org.geoshell.vector

import geoscript.layer.Layer
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.junit.Test
import org.springframework.shell.core.MethodTarget

import java.lang.reflect.Method

import static org.junit.Assert.*
import org.springframework.shell.core.Completion

class LayerNameConverterTest {

    @Test
    void supports() {
        LayerNameConverter converter = new LayerNameConverter()
        assertTrue converter.supports(LayerName, "cities")
        assertFalse converter.supports(WorkspaceName, "shps")
    }

    @Test
    void convertFromText() {
        LayerNameConverter converter = new LayerNameConverter()
        assertEquals new LayerName("cities"), converter.convertFromText("cities", LayerName, "")
    }

    @Test
    void getAllPossibleValues() {
        Catalog catalog = new Catalog()

        Workspace workspace1 = new Memory()
        workspace1.add(new Layer("cities"))
        workspace1.add(new Layer("roads"))

        Workspace workspace2 = new Memory()
        workspace2.add(new Layer("hospitals"))
        workspace2.add(new Layer("rivers"))

        catalog.layers[new LayerName("cities")] = workspace1.get("cities")
        catalog.layers[new LayerName("roads")] = workspace1.get("roads")
        catalog.layers[new LayerName("hospitals")] = workspace2.get("hospitals")
        catalog.layers[new LayerName("rivers")] = workspace2.get("rivers")

        catalog.workspaces[new WorkspaceName("ws1")] = workspace1
        catalog.workspaces[new WorkspaceName("ws2")] = workspace2

        Method method = LayerCommands.class.getDeclaredMethod("centroids", LayerName, WorkspaceName, String)

        // No workspace
        MethodTarget target = new MethodTarget(method, new LayerCommands(), "", "key")

        LayerNameConverter converter = new LayerNameConverter(catalog: catalog)
        List<Completion> completions = []
        assertTrue converter.getAllPossibleValues(completions, LayerName, "", "", target)
        assertTrue new Completion("cities") in completions
        assertTrue new Completion("roads") in completions
        assertTrue new Completion("hospitals") in completions
        assertTrue new Completion("rivers") in completions

        // Workspace 1
        target = new MethodTarget(method, new LayerCommands(), "--workspace ws1 --layer ", "key")

        converter = new LayerNameConverter(catalog: catalog)
        completions = []
        assertTrue converter.getAllPossibleValues(completions, LayerName, "", "", target)
        assertTrue new Completion("cities") in completions
        assertTrue new Completion("roads") in completions
        assertFalse new Completion("hospitals") in completions
        assertFalse new Completion("rivers") in completions

        // Workspace 2
        target = new MethodTarget(method, new LayerCommands(), "--workspace ws2 --layer ", "key")

        converter = new LayerNameConverter(catalog: catalog)
        completions = []
        assertTrue converter.getAllPossibleValues(completions, LayerName, "", "", target)
        assertFalse new Completion("cities") in completions
        assertFalse new Completion("roads") in completions
        assertTrue new Completion("hospitals") in completions
        assertTrue new Completion("rivers") in completions
    }

}
