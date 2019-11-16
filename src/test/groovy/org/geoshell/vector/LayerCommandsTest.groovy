package org.geoshell.vector

import geoscript.feature.Field
import geoscript.feature.Schema
import geoscript.geom.Bounds
import geoscript.geom.Polygon
import geoscript.layer.Layer
import geoscript.layer.Property
import geoscript.layer.Shapefile
import geoscript.workspace.Directory
import geoscript.workspace.GeoPackage
import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.springframework.shell.support.util.OsUtils
import static org.junit.Assert.*

class LayerCommandsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()

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

    @Test void remove() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        workspace.add(new Layer("lines"))
        workspace.add(new Layer("polygons"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace

        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.remove(new WorkspaceName("shapes"), new LayerName("points"))
        assertEquals "Layer points removed from Workspace shapes", result
        assertFalse workspace.has("points")
        assertTrue workspace.has("lines")
        assertTrue workspace.has("polygons")
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
        String expected = '''  Name                  Type
  --------------------  --------------------
  the_geom              Point
  id                    Integer
'''
        assertEquals expected, actual
    }

    @Test
    void features() {
        Catalog catalog = new Catalog()
        File file = new File(getClass().getClassLoader().getResource("points.shp").toURI())
        catalog.workspaces[new WorkspaceName("shps")] = new Directory(file.parentFile)
        catalog.layers[new LayerName("points")] = catalog.workspaces[new WorkspaceName("shps")].get("points")

        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String actual = cmds.features(new LayerName("points"), null, null, -1, -1, null)
        String expected =  OsUtils.LINE_SEPARATOR + "Feature (points.1)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-168.82415307349638 3.2542458437219324)" + OsUtils.LINE_SEPARATOR +
        "id = 0" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.2)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-29.05485512093759 -84.85394410965716)" + OsUtils.LINE_SEPARATOR +
        "id = 1" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.3)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (170.8801889092478 77.307673697752)" + OsUtils.LINE_SEPARATOR +
        "id = 2" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.4)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-80.6003065416709 -27.232156337089762)" + OsUtils.LINE_SEPARATOR +
        "id = 3" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.5)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (140.16217123004554 20.773543456240972)" + OsUtils.LINE_SEPARATOR +
        "id = 4" + OsUtils.LINE_SEPARATOR +
        ""  + OsUtils.LINE_SEPARATOR +
        "Feature (points.6)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (82.61838424737834 -62.32666922552009)" + OsUtils.LINE_SEPARATOR +
        "id = 5" + OsUtils.LINE_SEPARATOR +
        ""  + OsUtils.LINE_SEPARATOR +
        "Feature (points.7)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (29.340616510718434 -40.07842578881176)" + OsUtils.LINE_SEPARATOR +
        "id = 6" + OsUtils.LINE_SEPARATOR +
        ""  + OsUtils.LINE_SEPARATOR +
        "Feature (points.8)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-88.77814145011972 80.51980466965685)" + OsUtils.LINE_SEPARATOR +
        "id = 7" + OsUtils.LINE_SEPARATOR +
        ""  + OsUtils.LINE_SEPARATOR +
        "Feature (points.9)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-33.553779479649506 37.54073095445483)" + OsUtils.LINE_SEPARATOR +
        "id = 8" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.10)" + OsUtils.LINE_SEPARATOR +
        "-------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (83.6500645884006 -76.41816736026094)" + OsUtils.LINE_SEPARATOR +
        "id = 9" + OsUtils.LINE_SEPARATOR + OsUtils.LINE_SEPARATOR
        assertEquals expected, actual

        // Filter
        actual = cmds.features(new LayerName("points"), "IN('points.4')", null, -1, -1, null)
        expected = OsUtils.LINE_SEPARATOR + "Feature (points.4)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-80.6003065416709 -27.232156337089762)" + OsUtils.LINE_SEPARATOR +
        "id = 3" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR
        assertEquals expected, actual

        // Start
        actual = cmds.features(new LayerName("points"), null, "id asc", 8, 10, null)
        expected = OsUtils.LINE_SEPARATOR + "Feature (points.9)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-33.553779479649506 37.54073095445483)" + OsUtils.LINE_SEPARATOR +
        "id = 8" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.10)" + OsUtils.LINE_SEPARATOR +
        "-------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (83.6500645884006 -76.41816736026094)" + OsUtils.LINE_SEPARATOR +
        "id = 9" + OsUtils.LINE_SEPARATOR + OsUtils.LINE_SEPARATOR
        assertEquals expected, actual

        // Start Max
        actual = cmds.features(new LayerName("points"), null, "id asc", 2, 4, null)
        expected = OsUtils.LINE_SEPARATOR + "Feature (points.3)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (170.8801889092478 77.307673697752)" + OsUtils.LINE_SEPARATOR +
        "id = 2" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.4)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-80.6003065416709 -27.232156337089762)" + OsUtils.LINE_SEPARATOR +
        "id = 3" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.5)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (140.16217123004554 20.773543456240972)" + OsUtils.LINE_SEPARATOR +
        "id = 4" + OsUtils.LINE_SEPARATOR +
        ""  + OsUtils.LINE_SEPARATOR +
        "Feature (points.6)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (82.61838424737834 -62.32666922552009)" + OsUtils.LINE_SEPARATOR +
        "id = 5" + OsUtils.LINE_SEPARATOR +
        ""  + OsUtils.LINE_SEPARATOR
        assertEquals expected, actual

        // Max
        actual = cmds.features(new LayerName("points"), null, "id asc", -1, 2, null)
        expected = OsUtils.LINE_SEPARATOR + "Feature (points.1)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-168.82415307349638 3.2542458437219324)" + OsUtils.LINE_SEPARATOR +
        "id = 0" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.2)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "the_geom = POINT (-29.05485512093759 -84.85394410965716)" + OsUtils.LINE_SEPARATOR +
        "id = 1" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR
        assertEquals expected, actual

        // Fields
        actual = cmds.features(new LayerName("points"), null, "id asc", -1, 2, "id")
        expected = OsUtils.LINE_SEPARATOR + "Feature (points.1)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "id = 0" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR +
        "Feature (points.2)" + OsUtils.LINE_SEPARATOR +
        "------------------" + OsUtils.LINE_SEPARATOR +
        "id = 1" + OsUtils.LINE_SEPARATOR +
        "" + OsUtils.LINE_SEPARATOR
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

    @Test void interiorPoint() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.layers[new LayerName("grid")] = layer
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)

        String result = cmds.interiorPoints(new LayerName("grid"), new WorkspaceName("mem"), "interiorPoints")
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("interiorPoints")]
        Layer centroidLayer = catalog.layers[new LayerName("interiorPoints")]
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

    @Test void gridRowColumn() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)

        String result = cmds.gridRowColumn(
                new WorkspaceName("mem"),
                "grid",
                10, 10,
                "-180,-90,180,90",
                "polygon",
                "EPSG:4326",
                "geom"
        )
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("grid")]
        Layer layer = catalog.layers[new LayerName("grid")]
        assertEquals 100, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void gridWidthHeight() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)

        String result = cmds.gridWidthHeight(
                new WorkspaceName("mem"),
                "grid",
                20, 20,
                "-180,-90,180,90",
                "polygon",
                "EPSG:4326",
                "geom"
        )
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("grid")]
        Layer layer = catalog.layers[new LayerName("grid")]
        assertEquals 162, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void getSetStyle() {
        Catalog catalog = new Catalog()
        Workspace workspace = new Memory()
        workspace.add(new Layer("points"))
        workspace.add(new Layer("lines"))
        workspace.add(new Layer("polygons"))
        catalog.workspaces[new WorkspaceName("shapes")] = workspace
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.open(new WorkspaceName("shapes"), new LayerName("points"), "points")

        File sldFile = folder.newFile("points.sld")

        String result = cmds.getStyle(new LayerName("points"), sldFile)
        assertTrue result.startsWith("points style written to")
        assertTrue result.endsWith("points.sld")

        result = cmds.getStyle(new LayerName("points"), null)
        assertTrue result.contains("<sld:StyledLayerDescriptor")

        result = cmds.setStyle(new LayerName("points"), sldFile)
        assertTrue result.startsWith("Style ")
        assertTrue result.endsWith("points.sld set on points")
    }

    @Test void copy() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.workspaces[new WorkspaceName("gpkg")] = new GeoPackage(folder.newFile("layers.gpkg"))
        LayerCommands cmds = new LayerCommands(catalog: catalog)

        // Create 100 random points
        String result = cmds.random(new WorkspaceName("mem"), "points", 100, "0,0,100,100", "EPSG:4326", "the_id", "geom", false, false, 0)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("points")]
        Layer layer = catalog.layers[new LayerName("points")]
        assertEquals 100, layer.count
        assertEquals "Point", layer.schema.geom.typ

        // Copy all points
        result = cmds.copy(new LayerName("points"), new WorkspaceName("gpkg"), "geopoints1", null, null, -1, -1, null)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("geopoints1")]
        layer = catalog.layers[new LayerName("geopoints1")]
        assertEquals 100, layer.count
        assertEquals "Point", layer.schema.geom.typ

        // Copy all points sorted by id
        result = cmds.copy(new LayerName("points"), new WorkspaceName("gpkg"), "geopoints2", null, "the_id desc", -1, -1, null)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("geopoints2")]
        layer = catalog.layers[new LayerName("geopoints2")]
        assertEquals 100, layer.count
        assertEquals "Point", layer.schema.geom.typ

        // Copy some points (filter)
        result = cmds.copy(new LayerName("points"), new WorkspaceName("gpkg"), "geopoints3", "the_id > 49", null, -1, -1, null)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("geopoints3")]
        layer = catalog.layers[new LayerName("geopoints3")]
        assertEquals 50, layer.count
        assertEquals "Point", layer.schema.geom.typ

        // Copy some points (start)
        result = cmds.copy(new LayerName("points"), new WorkspaceName("gpkg"), "geopoints4", null, null, 80, 100, null)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("geopoints4")]
        layer = catalog.layers[new LayerName("geopoints4")]
        assertEquals 20, layer.count
        assertEquals "Point", layer.schema.geom.typ

        // Copy some points (start and max)
        result = cmds.copy(new LayerName("points"), new WorkspaceName("gpkg"), "geopoints5", null, null, 80, 10, null)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("geopoints5")]
        layer = catalog.layers[new LayerName("geopoints5")]
        assertEquals 10, layer.count
        assertEquals "Point", layer.schema.geom.typ

        // Copy some points (max)
        result = cmds.copy(new LayerName("points"), new WorkspaceName("gpkg"), "geopoints6", null, null, 0, 10, null)
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("geopoints6")]
        layer = catalog.layers[new LayerName("geopoints6")]
        assertEquals 10, layer.count
        assertEquals "Point", layer.schema.geom.typ

        // Copy all but only one field
        result = cmds.copy(new LayerName("points"), new WorkspaceName("gpkg"), "geopoints7", null, null, -1, -1, "geom")
        assertEquals "Done!", result

        assertNotNull catalog.layers[new LayerName("geopoints7")]
        layer = catalog.layers[new LayerName("geopoints7")]
        assertEquals 100, layer.count
        assertEquals "Point", layer.schema.geom.typ
        assertTrue layer.schema.has("geom")
        assertFalse layer.schema.has("the_id")
    }

    @Test void extent() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.extent(new LayerName("points"), new WorkspaceName("mem"), "extent", "geom")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("extent")]
        layer = catalog.layers[new LayerName("extent")]
        assertEquals 1, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void extents() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.buffer(new LayerName("points"), new WorkspaceName("mem"), "buffer", 10)
        String result = cmds.extents(new LayerName("buffer"), new WorkspaceName("mem"), "extents")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("extents")]
        Layer outLayer = catalog.layers[new LayerName("extents")]
        assertEquals layer.count, outLayer.count
        assertEquals "Polygon", outLayer.schema.geom.typ
    }

    @Test void convexhull() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.convexhull(new LayerName("points"), new WorkspaceName("mem"), "convexhull", "geom")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("convexhull")]
        layer = catalog.layers[new LayerName("convexhull")]
        assertEquals 1, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void convexhulls() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.buffer(new LayerName("points"), new WorkspaceName("mem"), "buffer", 10)
        String result = cmds.convexhulls(new LayerName("buffer"), new WorkspaceName("mem"), "convexhulls")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("convexhulls")]
        Layer outLayer = catalog.layers[new LayerName("convexhulls")]
        assertEquals layer.count, outLayer.count
        assertEquals "Polygon", outLayer.schema.geom.typ
    }

    @Test void voronoi() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.voronoi(new LayerName("points"), new WorkspaceName("mem"), "voronoi", "geom")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("voronoi")]
        layer = catalog.layers[new LayerName("voronoi")]
        assertEquals 10, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void delaunay() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.delaunay(new LayerName("points"), new WorkspaceName("mem"), "delaunay", "geom")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("delaunay")]
        layer = catalog.layers[new LayerName("delaunay")]
        assertEquals 12, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void coordinates() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.buffer(new LayerName("points"), new WorkspaceName("mem"), "buffer", 10)
        String result = cmds.coordinates(new LayerName("buffer"), new WorkspaceName("mem"), "coordinates")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("coordinates")]
        Layer outLayer = catalog.layers[new LayerName("coordinates")]
        assertEquals 330, outLayer.count
        assertEquals "Point", outLayer.schema.geom.typ
    }

    @Test void mincircle() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.mincircle(new LayerName("points"), new WorkspaceName("mem"), "mincircle", "geom")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("mincircle")]
        layer = catalog.layers[new LayerName("mincircle")]
        assertEquals 1, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void mincircles() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.buffer(new LayerName("points"), new WorkspaceName("mem"), "buffer", 10)
        String result = cmds.mincircles(new LayerName("buffer"), new WorkspaceName("mem"), "mincircles")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("mincircles")]
        Layer outLayer = catalog.layers[new LayerName("mincircles")]
        assertEquals layer.count, outLayer.count
        assertEquals "Polygon", outLayer.schema.geom.typ
    }

    @Test void create() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.create(new WorkspaceName("mem"), "points", "geom=Point EPSG:4326|id=Int|name=String")
        assertEquals "Created Layer points!", result
        assertNotNull catalog.layers[new LayerName("points")]
        Layer outLayer = catalog.layers[new LayerName("points")]
        assertEquals "Point", outLayer.schema.geom.typ
        ["geom","id","name"].each { String fld ->
            assertTrue outLayer.schema.has(fld)
        }
    }

    @Test void validity() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Layer invalidLayer = new Property(new File(getClass().getClassLoader().getResource("invalid.properties").toURI()))
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        catalog.layers[new LayerName("invalid")] = invalidLayer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.validity(new LayerName("points"),"id")
        assertEquals "No invalid geometries!", result
        result = cmds.validity(new LayerName("invalid"),"id,name")
        assertEquals """  Values                Reason
  --------------------  --------------------
  1,Polygon 1           Self-intersection
""", result
    }

    @Test void octagonalenvelope() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.octagonalenvelope(new LayerName("points"), new WorkspaceName("mem"), "octagonalenvelope", "geom")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("octagonalenvelope")]
        layer = catalog.layers[new LayerName("octagonalenvelope")]
        assertEquals 1, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void octagonalenvelopes() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.buffer(new LayerName("points"), new WorkspaceName("mem"), "buffer", 10)
        String result = cmds.octagonalenvelopes(new LayerName("buffer"), new WorkspaceName("mem"), "octagonalenvelopes")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("octagonalenvelopes")]
        Layer outLayer = catalog.layers[new LayerName("octagonalenvelopes")]
        assertEquals layer.count, outLayer.count
        assertEquals "Polygon", outLayer.schema.geom.typ
    }

    @Test void add() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.create(new WorkspaceName("mem"), "points", "geom=Point EPSG:4326|id=Int|name=String")
        String result = cmds.count(new LayerName("points"))
        assertEquals "0", result
        result = cmds.add(new LayerName("points"), "geom=POINT(1 1)|id=1|name=Home")
        assertEquals "Added Feature to points", result
        result = cmds.count(new LayerName("points"))
        assertEquals "1", result
        Layer layer = catalog.layers[new LayerName("points")]
        assertEquals 1, layer.count
        assertEquals "POINT (1 1)", layer.features[0].geom.wkt
        assertEquals 1, layer.features[0].get("id")
        assertEquals "Home", layer.features[0].get("name")
        result = cmds.add(new LayerName("points"), "geom=POINT(2 2)|id=2|name=Work")
        assertEquals "Added Feature to points", result
        result = cmds.count(new LayerName("points"))
        assertEquals "2", result
        assertEquals 2, layer.count
        assertEquals "POINT (2 2)", layer.features[1].geom.wkt
        assertEquals 2, layer.features[1].get("id")
        assertEquals "Work", layer.features[1].get("name")
    }

    @Test void minrect() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.minrect(new LayerName("points"), new WorkspaceName("mem"), "minrect", "geom")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("minrect")]
        layer = catalog.layers[new LayerName("minrect")]
        assertEquals 1, layer.count
        assertEquals "Polygon", layer.schema.geom.typ
    }

    @Test void minrects() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.buffer(new LayerName("points"), new WorkspaceName("mem"), "buffer", 10)
        String result = cmds.minrects(new LayerName("buffer"), new WorkspaceName("mem"), "minrects")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("minrects")]
        Layer outLayer = catalog.layers[new LayerName("minrects")]
        assertEquals layer.count, outLayer.count
        assertEquals "Polygon", outLayer.schema.geom.typ
    }

    @Test void addareafield() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("grid")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.addareafield(new LayerName("grid"), new WorkspaceName("mem"), "grid_area", "area")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("grid_area")]
        Layer outLayer = catalog.layers[new LayerName("grid_area")]
        assertEquals layer.count, outLayer.count
        assertEquals layer.schema.geom.typ, outLayer.schema.geom.typ
        assertFalse layer.schema.has("area")
        assertTrue outLayer.schema.has("area")
    }

    @Test void addidfield() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("grid")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.addidfield(new LayerName("grid"), new WorkspaceName("mem"), "grid_id", "g_id", 1)
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("grid_id")]
        Layer outLayer = catalog.layers[new LayerName("grid_id")]
        assertEquals layer.count, outLayer.count
        assertEquals layer.schema.geom.typ, outLayer.schema.geom.typ
        assertFalse layer.schema.has("g_id")
        assertTrue outLayer.schema.has("g_id")
    }

    @Test void addxyfields() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.addxyfields(new LayerName("points"), new WorkspaceName("mem"), "points_xy", "x", "y")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("points_xy")]
        Layer outLayer = catalog.layers[new LayerName("points_xy")]
        assertEquals layer.count, outLayer.count
        assertEquals layer.schema.geom.typ, outLayer.schema.geom.typ
        assertFalse layer.schema.has("x")
        assertTrue outLayer.schema.has("x")
        assertFalse layer.schema.has("y")
        assertTrue outLayer.schema.has("y")
    }

    @Test void addfields() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.addfields(new LayerName("points"), new WorkspaceName("mem"), "points_xy", "xcol=Double,ycol=Double")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("points_xy")]
        Layer outLayer = catalog.layers[new LayerName("points_xy")]
        assertEquals layer.count, outLayer.count
        assertEquals layer.schema.geom.typ, outLayer.schema.geom.typ
        assertFalse layer.schema.has("xcol")
        assertTrue outLayer.schema.has("xcol")
        assertFalse layer.schema.has("ycol")
        assertTrue outLayer.schema.has("ycol")
    }

    @Test void removefields() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        // Add Fields
        String result = cmds.addfields(new LayerName("points"), new WorkspaceName("mem"), "points_xy", "xcol=Double,ycol=Double")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("points_xy")]
        Layer outLayer = catalog.layers[new LayerName("points_xy")]
        assertEquals layer.count, outLayer.count
        assertEquals layer.schema.geom.typ, outLayer.schema.geom.typ
        assertFalse layer.schema.has("xcol")
        assertTrue outLayer.schema.has("xcol")
        assertFalse layer.schema.has("ycol")
        assertTrue outLayer.schema.has("ycol")
        // Remove Fields
        result = cmds.removefields(new LayerName("points_xy"), new WorkspaceName("mem"), "points_no_xy", "xcol,ycol")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("points_no_xy")]
        outLayer = catalog.layers[new LayerName("points_no_xy")]
        assertEquals layer.count, outLayer.count
        assertEquals layer.schema.geom.typ, outLayer.schema.geom.typ
        assertFalse outLayer.schema.has("xcol")
        assertFalse outLayer.schema.has("ycol")
    }

    @Test void simplify() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.buffer(new LayerName("points"), new WorkspaceName("mem"), "polys", 10)
        String result = cmds.simplify(new LayerName("polys"), new WorkspaceName("mem"), "polys_simplified", "tp", 4)
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("polys_simplified")]
        Layer outLayer = catalog.layers[new LayerName("polys_simplified")]
        Layer bufferLayer = catalog.layers[new LayerName("polys")]
        assertEquals layer.count, outLayer.count
        assertEquals "Polygon", outLayer.schema.geom.typ
        (0..<layer.count).each { int i ->
            assertTrue outLayer.features[i].geom.numPoints < bufferLayer.features[i].geom.numPoints
        }
    }

    @Test void densify() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("grid.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("grid")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.densify(new LayerName("grid"), new WorkspaceName("mem"), "grid_densified", 10)
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("grid_densified")]
        Layer outLayer = catalog.layers[new LayerName("grid_densified")]
        assertEquals layer.count, outLayer.count
        assertEquals "MultiPolygon", outLayer.schema.geom.typ
        (0..<layer.count).each { int i ->
            assertTrue outLayer.features[i].geom.numPoints > layer.features[i].geom.numPoints
        }
    }

    @Test void delete() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        cmds.create(new WorkspaceName("mem"), "points", "geom=Point EPSG:4326|fid=Int|name=String")
        cmds.add(new LayerName("points"), "geom=POINT(1 1)|fid=1|name=Home")
        cmds.add(new LayerName("points"), "geom=POINT(2 2)|fid=2|name=Work")
        assertEquals "2", cmds.count(new LayerName("points"))
        String result = cmds.delete(new LayerName("points"),"fid=2")
        assertEquals "Deleted fid=2 Features from points", result
        assertEquals "1", cmds.count(new LayerName("points"))
        result = cmds.delete(new LayerName("points"),"fid=1")
        assertEquals "Deleted fid=1 Features from points", result
        assertEquals "0", cmds.count(new LayerName("points"))
    }

    @Test void updatefields() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("points.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        // Add Fields
        String result = cmds.addfields(new LayerName("points"), new WorkspaceName("mem"), "points1", "xcol=Double,ycol=Double,label=String")
        assertEquals "Done!", result
        assertNotNull catalog.layers[new LayerName("points1")]
        Layer outLayer = catalog.layers[new LayerName("points1")]
        assertEquals layer.count, outLayer.count
        assertEquals layer.schema.geom.typ, outLayer.schema.geom.typ
        assertFalse layer.schema.has("xcol")
        assertTrue outLayer.schema.has("xcol")
        assertFalse layer.schema.has("ycol")
        assertTrue outLayer.schema.has("ycol")
        // Update new Fields with values
        result = cmds.updatefield(new LayerName("points1"), "label", "value", "INCLUDE", false)
        assertEquals "Done updating label with value!", result
        result = cmds.updatefield(new LayerName("points1"), "xcol", "return f.geom.centroid.x", "INCLUDE", true)
        assertEquals "Done updating xcol with return f.geom.centroid.x!", result
    }

    @Test void transform() {
        Layer layer = new Property(new File(getClass().getClassLoader().getResource("points.properties").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("points")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.transform(new LayerName("points"), new WorkspaceName("mem"), "points_transformed", "the_geom=buffer(the_geom, 10)|name=strToUpperCase(name)|distance=distance*10")
        assertEquals "Done transforming points to points_transformed with the_geom=buffer(the_geom, 10)|name=strToUpperCase(name)|distance=distance*10!", result
        Layer transformedLayer = catalog.layers[new LayerName("points_transformed")]
        List features1 = layer.features
        List features2 = transformedLayer.features
        // 1
        assertTrue(features2[0].geom instanceof Polygon)
        assertEquals(features1[0].get("name").toString().toUpperCase(), features2[0].get("name"))
        assertEquals((features1[0].get("distance") as double) * 10, features2[0].get("distance") as double, 0.1)
        // 2
        assertTrue(features2[1].geom instanceof Polygon)
        assertEquals(features1[1].get("name").toString().toUpperCase(), features2[1].get("name"))
        assertEquals((features1[1].get("distance") as double) * 10, features2[1].get("distance") as double, 0.1)
        // 3
        assertTrue(features2[2].geom instanceof Polygon)
        assertEquals(features1[2].get("name").toString().toUpperCase(), features2[2].get("name"))
        assertEquals((features1[2].get("distance") as double) * 10, features2[2].get("distance") as double, 0.1)
    }

    @Test void reproject() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("states.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("states")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        assertEquals "EPSG:4326", cmds.projection(new LayerName("states"))
        String result = cmds.reproject(new LayerName("states"), new WorkspaceName("mem"), "states_reprojected", "EPSG:3857")
        assertEquals "Done reprojecting states to states_reprojected in EPSG:3857!", result
        assertEquals "EPSG:3857", cmds.projection(new LayerName("states_reprojected"))
        Layer reprojectedLayer = catalog.layers[new LayerName("states_reprojected")]
        assertNotNull reprojectedLayer
        assertEquals layer.count, reprojectedLayer.count
        assertEquals "EPSG:3857", reprojectedLayer.proj.id
    }

    @Test void projection() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("states.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("states")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.projection(new LayerName("states"))
        assertEquals "EPSG:4326", result
    }

    /**
     * Create two Layers for Layer Algebra testing based on the GDAL spec:
     * http://trac.osgeo.org/gdal/wiki/LayerAlgebra
     * @return A List of two Layers
     */
    private void createGdalLayerAlgebraTestLayers(Catalog catalog) {
        Workspace workspace = new Memory()
        catalog.workspaces[new WorkspaceName("mem")] = workspace

        Layer layer1 = workspace.create(new Schema("a",[new Field("the_geom", "Polygon"), new Field("A","int")]))
        Bounds b1 = new Bounds(90, 100, 100, 110)
        Bounds b2 = new Bounds(120, 100, 130, 110)
        layer1.add([the_geom: b1.geometry, A: 1])
        layer1.add([the_geom: b2.geometry, A: 2])
        catalog.layers[new LayerName("a")] = layer1

        Layer layer2 = workspace.create(new Schema("b",[new Field("the_geom", "Polygon"), new Field("B","int")]))
        Bounds b3 = new Bounds(85, 95, 95, 105)
        Bounds b4 = new Bounds(97, 95, 125, 105)
        layer2.add([the_geom: b3.geometry, B: 3])
        layer2.add([the_geom: b4.geometry, B: 4])
        catalog.layers[new LayerName("b")] = layer2
    }

    @Test void clip() {
        Catalog catalog = new Catalog()
        createGdalLayerAlgebraTestLayers(catalog)
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.clip(new LayerName("a"), new LayerName("b"), new WorkspaceName("mem"), "clipped")
        assertEquals "Done clipping a to b to create clipped!", result
        Layer layer = catalog.layers[new LayerName("clipped")]
        // Check schema
        assertEquals "clipped", layer.name
        assertTrue layer.schema.has("A")
        assertFalse layer.schema.has("B")
        assertEquals "Polygon", layer.schema.geom.typ
        // Check features
        assertEquals 3, layer.count
        assertEquals 2, layer.count("A = 1")
        assertEquals 1, layer.count("A = 2")
        assertEquals "POLYGON ((90 100, 90 105, 95 105, 95 100, 90 100))", layer.getFeatures("A = 1")[0].geom.wkt
        assertEquals "POLYGON ((100 105, 100 100, 97 100, 97 105, 100 105))", layer.getFeatures("A = 1")[1].geom.wkt
        assertEquals "POLYGON ((120 100, 120 105, 125 105, 125 100, 120 100))", layer.getFeatures("A = 2")[0].geom.wkt
    }

    @Test void union() {
        Catalog catalog = new Catalog()
        createGdalLayerAlgebraTestLayers(catalog)
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.union(new LayerName("a"), new LayerName("b"), new WorkspaceName("mem"), "a_b_union", false, true)
        assertEquals "Done unioning a and b to create a_b_union!", result
        Layer layer = catalog.layers[new LayerName("a_b_union")]
        // Check schema
        assertEquals "a_b_union", layer.name
        assertTrue layer.schema.has("A")
        assertTrue layer.schema.has("B")
        assertEquals "Polygon", layer.schema.geom.typ
        // Check features
        assertEquals 7, layer.count
        assertEquals 1, layer.count("A = 1 AND B = 3")
        assertEquals 1, layer.count("A = 1 AND B = 4")
        assertEquals 1, layer.count("A = 1 AND B IS NULL")
        assertEquals 1, layer.count("A = 2 AND B = 4")
        assertEquals 1, layer.count("A = 2 AND B IS NULL")
        assertEquals 1, layer.count("A IS NULL AND B = 3")
        assertEquals 1, layer.count("A IS NULL AND B = 4")
        assertEquals "POLYGON ((90 100, 90 105, 95 105, 95 100, 90 100))", layer.getFeatures("A = 1 AND B = 3")[0].geom.wkt
        assertEquals "POLYGON ((100 105, 100 100, 97 100, 97 105, 100 105))", layer.getFeatures("A = 1 AND B = 4")[0].geom.wkt
        assertEquals "POLYGON ((90 105, 90 110, 100 110, 100 105, 97 105, 97 100, 95 100, 95 105, 90 105))", layer.getFeatures("A = 1 AND B IS NULL")[0].geom.wkt
        assertEquals "POLYGON ((120 100, 120 105, 125 105, 125 100, 120 100))", layer.getFeatures("A = 2 AND B = 4")[0].geom.wkt
        assertEquals "POLYGON ((120 105, 120 110, 130 110, 130 100, 125 100, 125 105, 120 105))", layer.getFeatures("A = 2 AND B IS NULL")[0].geom.wkt
        assertEquals "POLYGON ((85 95, 85 105, 90 105, 90 100, 95 100, 95 95, 85 95))", layer.getFeatures("A IS NULL AND B = 3")[0].geom.wkt
        assertEquals "POLYGON ((97 95, 97 100, 100 100, 100 105, 120 105, 120 100, 125 100, 125 95, 97 95))", layer.getFeatures("A IS NULL AND B = 4")[0].geom.wkt
    }

    @Test void intersection() {
        Catalog catalog = new Catalog()
        createGdalLayerAlgebraTestLayers(catalog)
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.intersection(new LayerName("a"), new LayerName("b"), new WorkspaceName("mem"), "a_b_intersection", false, true)
        assertEquals "Done calculating the intersection between a and b to create a_b_intersection!", result
        Layer layer = catalog.layers[new LayerName("a_b_intersection")]
        // Check schema
        assertEquals "a_b_intersection", layer.name
        assertTrue layer.schema.has("A")
        assertTrue layer.schema.has("B")
        assertEquals "Polygon", layer.schema.geom.typ
        // Check features
        assertEquals 3, layer.count
        assertEquals 1, layer.count("A = 1 AND B = 3")
        assertEquals 1, layer.count("A = 1 AND B = 4")
        assertEquals 1, layer.count("A = 2 AND B = 4")
        assertEquals "POLYGON ((90 100, 90 105, 95 105, 95 100, 90 100))", layer.getFeatures("A = 1 AND B = 3")[0].geom.wkt
        assertEquals "POLYGON ((100 105, 100 100, 97 100, 97 105, 100 105))", layer.getFeatures("A = 1 AND B = 4")[0].geom.wkt
        assertEquals "POLYGON ((120 100, 120 105, 125 105, 125 100, 120 100))", layer.getFeatures("A = 2 AND B = 4")[0].geom.wkt
    }

    @Test void erase() {
        Catalog catalog = new Catalog()
        createGdalLayerAlgebraTestLayers(catalog)
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.erase(new LayerName("a"), new LayerName("b"), new WorkspaceName("mem"), "a_b_erase")
        assertEquals "Done erasing a from b to create a_b_erase!", result
        Layer layer = catalog.layers[new LayerName("a_b_erase")]
        // Check schema
        assertEquals "a_b_erase", layer.name
        assertTrue layer.schema.has("A")
        assertFalse layer.schema.has("B")
        assertEquals "Polygon", layer.schema.geom.typ
        // Check features
        assertEquals 2, layer.count
        assertEquals 1, layer.count("A = 1")
        assertEquals 1, layer.count("A = 2")
        assertEquals "POLYGON ((90 105, 90 110, 100 110, 100 105, 97 105, 97 100, 95 100, 95 105, 90 105))", layer.getFeatures("A = 1")[0].geom.wkt
        assertEquals "POLYGON ((120 105, 120 110, 130 110, 130 100, 125 100, 125 105, 120 105))", layer.getFeatures("A = 2")[0].geom.wkt
    }

    @Test void identity() {
        Catalog catalog = new Catalog()
        createGdalLayerAlgebraTestLayers(catalog)
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.identity(new LayerName("a"), new LayerName("b"), new WorkspaceName("mem"), "a_b_identity", false, true)
        assertEquals "Done calculating the identity between a and b to create a_b_identity!", result
        Layer layer = catalog.layers[new LayerName("a_b_identity")]
        // Check schema
        assertEquals "a_b_identity", layer.name
        assertTrue layer.schema.has("A")
        assertTrue layer.schema.has("B")
        assertEquals "Polygon", layer.schema.geom.typ
        // Check features
        assertEquals 5, layer.count
        assertEquals 1, layer.count("A = 1 AND B = 3")
        assertEquals 1, layer.count("A = 1 AND B = 4")
        assertEquals 1, layer.count("A = 1 AND B IS NULL")
        assertEquals 1, layer.count("A = 2 AND B = 4")
        assertEquals 1, layer.count("A = 2 AND B IS NULL")
        assertEquals "POLYGON ((90 100, 90 105, 95 105, 95 100, 90 100))", layer.getFeatures("A = 1 AND B = 3")[0].geom.wkt
        assertEquals "POLYGON ((100 105, 100 100, 97 100, 97 105, 100 105))", layer.getFeatures("A = 1 AND B = 4")[0].geom.wkt
        assertEquals "POLYGON ((90 105, 90 110, 100 110, 100 105, 97 105, 97 100, 95 100, 95 105, 90 105))", layer.getFeatures("A = 1 AND B IS NULL")[0].geom.wkt
        assertEquals "POLYGON ((120 100, 120 105, 125 105, 125 100, 120 100))", layer.getFeatures("A = 2 AND B = 4")[0].geom.wkt
        assertEquals "POLYGON ((120 105, 120 110, 130 110, 130 100, 125 100, 125 105, 120 105))", layer.getFeatures("A = 2 AND B IS NULL")[0].geom.wkt
    }

    @Test void update() {
        Catalog catalog = new Catalog()
        createGdalLayerAlgebraTestLayers(catalog)
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.update(new LayerName("a"), new LayerName("b"), new WorkspaceName("mem"), "a_b_update", )
        assertEquals "Done calculating the update between a and b to create a_b_update!", result
        Layer layer = catalog.layers[new LayerName("a_b_update")]
        // Check schema
        assertEquals "a_b_update", layer.name
        assertTrue layer.schema.has("A")
        assertFalse layer.schema.has("B")
        assertEquals "Polygon", layer.schema.geom.typ
        // Check features
        assertEquals 4, layer.count
        assertEquals 1, layer.count("A = 1")
        assertEquals 1, layer.count("A = 2")
        assertEquals 2, layer.count("A IS NULL")
        assertEquals "POLYGON ((90 105, 90 110, 100 110, 100 105, 97 105, 97 100, 95 100, 95 105, 90 105))", layer.getFeatures("A = 1")[0].geom.wkt
        assertEquals "POLYGON ((120 105, 120 110, 130 110, 130 100, 125 100, 125 105, 120 105))", layer.getFeatures("A = 2")[0].geom.wkt
        assertEquals "POLYGON ((85 95, 85 105, 95 105, 95 95, 85 95))", layer.getFeatures("A IS NULL")[0].geom.wkt
        assertEquals "POLYGON ((97 95, 97 105, 125 105, 125 95, 97 95))", layer.getFeatures("A IS NULL")[1].geom.wkt
    }

    @Test void symDifference() {
        Catalog catalog = new Catalog()
        createGdalLayerAlgebraTestLayers(catalog)
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.symDifference(new LayerName("a"), new LayerName("b"), new WorkspaceName("mem"), "a_b_symdifference", false, true)
        assertEquals "Done calculating the symmetric difference between a and b to create a_b_symdifference!", result
        Layer layer = catalog.layers[new LayerName("a_b_symdifference")]
        // Check schema
        assertEquals "a_b_symdifference", layer.name
        assertTrue layer.schema.has("A")
        assertTrue layer.schema.has("B")
        assertEquals "Polygon", layer.schema.geom.typ
        // Check features
        assertEquals 4, layer.count
        assertEquals 1, layer.count("A = 1 AND B IS NULL")
        assertEquals 1, layer.count("A = 2 AND B IS NULL")
        assertEquals 1, layer.count("A IS NULL AND B = 3")
        assertEquals 1, layer.count("A IS NULL AND B = 4")
        assertEquals "POLYGON ((90 105, 90 110, 100 110, 100 105, 97 105, 97 100, 95 100, 95 105, 90 105))",layer.getFeatures("A = 1 AND B IS NULL")[0].geom.wkt
        assertEquals "POLYGON ((120 105, 120 110, 130 110, 130 100, 125 100, 125 105, 120 105))", layer.getFeatures("A = 2 AND B IS NULL")[0].geom.wkt
        assertEquals "POLYGON ((85 95, 85 105, 90 105, 90 100, 95 100, 95 95, 85 95))", layer.getFeatures("A IS NULL AND B = 3")[0].geom.wkt
        assertEquals "POLYGON ((97 95, 97 100, 100 100, 100 105, 120 105, 120 100, 125 100, 125 95, 97 95))", layer.getFeatures("A IS NULL AND B = 4")[0].geom.wkt
    }

    @Test void dissolve() {
        Layer layer = new Shapefile(new File(getClass().getClassLoader().getResource("states.shp").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("states")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.dissolve(
            new LayerName("states"),
            new WorkspaceName("mem"),
            "regions",
            "SUB_REGION",
            "id",
            "count"
        )
        assertEquals "Done dissolving states to regions by SUB_REGION!", result
        Layer dissolvedLayer = catalog.layers[new LayerName("regions")]
        assertEquals 9, dissolvedLayer.count
    }

    @Test void pointsAlongLine() {
        Layer layer = new Property(new File(getClass().getClassLoader().getResource("lines.properties").toURI()))
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        catalog.layers[new LayerName("lines")] = layer
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.pointsAlongLine(
                new LayerName("lines"),
                new WorkspaceName("mem"),
                "points",
                1.0
        )
        assertEquals "Done placing points along lines every 1.0 to create points!", result
        Layer pointsLayer = catalog.layers[new LayerName("points")]
        assertEquals 20, pointsLayer.count
    }

    @Test void createOvalGraticule() {
        Catalog catalog = new Catalog()
        catalog.workspaces[new WorkspaceName("mem")] = new Memory()
        LayerCommands cmds = new LayerCommands(catalog: catalog)
        String result = cmds.createOvalGraticule(
                new WorkspaceName("mem"),
                "ovals",
                "-180,-90,180,90",
                10,
        )
        assertEquals "Created Graticule Layer ovals!", result
        Layer layer = catalog.layers[new LayerName("ovals")]
        assertEquals 648, layer.count
    }
}
