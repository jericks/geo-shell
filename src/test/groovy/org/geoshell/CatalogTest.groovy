package org.geoshell

import geoscript.layer.Format
import geoscript.layer.Layer
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.raster.FormatName
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

class CatalogTest {

    @Test void workspaces() {
        Catalog catalog = new Catalog()
        // Should be empty
        assertTrue catalog.workspaces.isEmpty()
        assertNull catalog.workspaces[new WorkspaceName("memory")]
        // Add
        catalog.workspaces[new WorkspaceName("memory")] = new Memory()
        // Should contain one
        assertFalse catalog.workspaces.isEmpty()
        assertNotNull catalog.workspaces[new WorkspaceName("memory")]
        // Destroy
        catalog.destroy()
        // Should be empty
        assertTrue catalog.workspaces.isEmpty()
        assertNull catalog.workspaces[new WorkspaceName("memory")]
    }

    @Test void layers() {
        Catalog catalog = new Catalog()
        // Should be empty
        assertTrue catalog.layers.isEmpty()
        assertNull catalog.layers[new LayerName("points")]
        // Add
        Workspace workspace = new Memory()
        Layer layer = workspace.create("points")
        catalog.workspaces[new WorkspaceName("memory")] = workspace
        catalog.layers[new LayerName("points")] = layer
        // Should contain one
        assertFalse catalog.layers.isEmpty()
        assertNotNull catalog.layers[new LayerName("points")]
        // Destroy
        catalog.destroy()
        // Should be empty
        assertTrue catalog.layers.isEmpty()
        assertNull catalog.layers[new LayerName("points")]
    }

    @Test void formats() {
        Catalog catalog = new Catalog()
        // Should be empty
        assertTrue catalog.formats.isEmpty()
        assertNull catalog.formats[new FormatName("raster")]
        // Add
        Format format = Format.getFormat(new File(getClass().getClassLoader().getResource("raster.tif").toURI()))
        catalog.formats[new FormatName("raster")] = format
        // Should contain one
        assertFalse catalog.formats.isEmpty()
        assertNotNull catalog.formats[new FormatName("raster")]
        // Destroy
        catalog.destroy()
        // Should be empty
        assertTrue catalog.formats.isEmpty()
        assertNull catalog.formats[new FormatName("raster")]
    }

}
