package org.geoshell.vector

import geoscript.feature.Feature
import geoscript.feature.Field
import geoscript.feature.Schema
import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.geom.GeometryCollection
import geoscript.geom.MultiPoint
import geoscript.geom.Point
import geoscript.layer.Layer
import geoscript.layer.Writer as LayerWriter
import geoscript.style.Style
import geoscript.style.io.CSSReader
import geoscript.style.io.SLDReader
import geoscript.style.io.SLDWriter
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.shell.support.table.Table
import org.springframework.shell.support.table.TableHeader
import org.springframework.shell.support.table.TableRow
import org.springframework.shell.support.util.OsUtils
import org.springframework.stereotype.Component

@Component
class LayerCommands implements CommandMarker {

    @Autowired
    Catalog catalog

    @CliCommand(value = "layer open", help = "Open a Layer.")
    String open(
            @CliOption(key = "workspace", mandatory = true, help = "The Workspace name") WorkspaceName workspaceName,
            @CliOption(key = "layer", mandatory = true, help = "The Layer name") LayerName layerName,
            @CliOption(key = "name", mandatory = false, help = "The name") String name
    ) throws Exception {
        if (!name) {
            name = "${workspaceName.name}:${layerName.name}"
        }
        Workspace workspace = catalog.workspaces[workspaceName]
        if (workspace) {
            if (workspace.has(layerName.name)) {
                Layer layer = workspace.get(layerName.name)
                catalog.layers[new LayerName(name)] = layer
                "Opened Workspace ${workspaceName.name} Layer ${layerName.name} as ${name}"
            } else {
                "Unable to find Layer ${layerName}"
            }
        } else {
            "Unable to find Workspace ${workspaceName}"
        }
    }

    @CliCommand(value = "layer close", help = "Close a Layer.")
    String close(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName name
    ) throws Exception {
        Layer layer = catalog.layers[name]
        if (layer) {
            catalog.layers.remove(name)
            "Layer ${name} closed!"
        } else {
            "Unable to find Layer ${name}"
        }
    }

    @CliCommand(value = "layer list", help = "List open Layers.")
    String list() throws Exception {
        catalog.layers.collect { LayerName name, Layer layer ->
            "${name} = ${layer.workspace.format}"
        }.join(OsUtils.LINE_SEPARATOR)
    }

    @CliCommand(value = "layer remove", help = "Remove a Layer from a Workspace.")
    String remove(
            @CliOption(key = "workspace", mandatory = true, help = "The Workspace name") WorkspaceName workspaceName,
            @CliOption(key = "layer", mandatory = true, help = "The Layer name") LayerName layerName
    ) throws Exception {
        Workspace workspace = catalog.workspaces[workspaceName]
        if (workspace) {
            if (workspace.has(layerName.name)) {
                close(layerName)
                workspace.remove(layerName.name)
                "Layer ${layerName.name} removed from Workspace ${workspaceName.name}"
            } else {
                "Unable to remove Layer ${layerName}"
            }
        } else {
            "Unable to find Workspace ${workspaceName}"
        }
    }

    @CliCommand(value = "layer count", help = "Count the Feature in a Layer.")
    String count(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName name
    ) throws Exception {
        Layer layer = catalog.layers[name]
        if (layer) {
            "${layer.count}"
        } else {
            "Unable to find Layer ${name}"
        }
    }

    @CliCommand(value = "layer schema", help = "Inspect a Layer's Schema.")
    String schema(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName name
    ) throws Exception {
        Layer layer = catalog.layers[name]
        if (layer) {
            Table table = new Table()
            table.addHeader(0, new TableHeader("Name", 20))
            table.addHeader(1, new TableHeader("Type", 20))
            layer.schema.fields.each { Field f ->
                TableRow row = table.newRow()
                row.addValue(0, f.name)
                row.addValue(1, f.typ)
            }
            table.calculateColumnWidths()
            table.toString()
        } else {
            "Unable to find Layer ${name}"
        }
    }

    @CliCommand(value = "layer style set", help = "Set a Layer's style")
    String setStyle(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName name,
            @CliOption(key = "style", mandatory = true, help = "The SLD or CSS File") File styleFile
    ) throws Exception {
        Layer layer = catalog.layers[name]
        if (layer) {
            Style style = null
            if (styleFile.name.endsWith(".sld")) {
                style = new SLDReader().read(styleFile)
            } else if (styleFile.name.endsWith(".css")) {
                style = new CSSReader().read(styleFile)
            }
            if (style) {
                layer.style = style
                "Style ${styleFile.absolutePath} set on ${name}"
            } else {
                "Unable to read ${styleFile.absolutePath}"
            }
        } else {
            "Unable to find Layer ${name}"
        }
    }

    @CliCommand(value = "layer style get", help = "Get the Layer's style.")
    String getStyle(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName name,
            @CliOption(key = "style", mandatory = false, help = "The SLD File") File styleFile
    ) throws Exception {
        Layer layer = catalog.layers[name]
        if (layer) {
            if (styleFile) {
                new SLDWriter().write(layer.style, styleFile)
                "${name} style written to ${styleFile}"
            } else {
                new SLDWriter().write(layer.style)
            }
        } else {
            "Unable to find Layer ${name}"
        }
    }

    @CliCommand(value = "layer buffer", help = "Buffer the input Layer to the output Layer.")
    String buffer(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "distance", mandatory = true, help = "The buffer distance") double distance
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                inputLayer.buffer(distance, outWorkspace: outputWorkspace, outLayer: outputLayerName)
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer centroid", help = "Calculate the centroids of the input Layer to the output Layer.")
    String centroids(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.changeGeometryType("Point", outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { LayerWriter writer ->
                    inputLayer.eachFeature { Feature f ->
                        Map values = [:]
                        f.attributes.each { k, v ->
                            if (v instanceof geoscript.geom.Geometry) {
                                values[k] = v.centroid
                            } else {
                                values[k] = v
                            }
                        }
                        writer.add(outputLayer.schema.feature(values, f.id))
                    }
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer interiorpoint", help = "Calculate the interior points of the input Layer to the output Layer.")
    String interiorPoints(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.changeGeometryType("Point", outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { LayerWriter writer ->
                    inputLayer.eachFeature { Feature f ->
                        Map values = [:]
                        f.attributes.each { k, v ->
                            if (v instanceof geoscript.geom.Geometry) {
                                values[k] = v.interiorPoint
                            } else {
                                values[k] = v
                            }
                        }
                        writer.add(outputLayer.schema.feature(values, f.id))
                    }
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer random", help = "Create a Layer with a number of randomly located points")
    String random(
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "number", mandatory = true, help = "The number of points") int numberOfPoints,
            @CliOption(key = "geometry", mandatory = true, help = "The geometry or bounds in which to create the points ") String geometry,
            @CliOption(key = "projection", mandatory = true, help = "The projection") String projection,
            @CliOption(key = "id-field", specifiedDefaultValue = "id", unspecifiedDefaultValue = "id", mandatory = false, help = "The id field name") String idFieldName,
            @CliOption(key = "geometry-field", specifiedDefaultValue = "the_geom", unspecifiedDefaultValue = "the_geom", mandatory = false, help = "The geometry field name") String geometryFieldName,
            @CliOption(key = "grid", specifiedDefaultValue = "false", unspecifiedDefaultValue = "false", mandatory = false, help = "Whether to create points in a grid")boolean grid,
            @CliOption(key = "constrained-to-circle", specifiedDefaultValue = "false", unspecifiedDefaultValue = "false", mandatory = false, help = "Whether points should be constrained to a circle")boolean constrainedToCircle,
            @CliOption(key = "gutter-fraction", specifiedDefaultValue = "0", unspecifiedDefaultValue = "0", mandatory = false, help = "The size of gutter between cells") int gutterFraction
    ) throws Exception {
      Workspace outputWorkspace = catalog.workspaces[workspaceName]
      if (outputWorkspace) {
          Schema schema = new Schema(outputLayerName, [
            new Field(geometryFieldName, "Point", projection),
            new Field(idFieldName, "int")
          ])
          Layer outputLayer = outputWorkspace.create(schema)
          outputLayer.withWriter { LayerWriter writer ->
              MultiPoint multiPoint
              if (grid) {
                  multiPoint = Geometry.createRandomPointsInGrid(Geometry.fromString(geometry), numberOfPoints, constrainedToCircle, gutterFraction)
              } else {
                  multiPoint = Geometry.createRandomPoints(Geometry.fromString(geometry), numberOfPoints)
              }
              multiPoint.points.eachWithIndex { Point pt, int i ->
                  Feature f = writer.newFeature
                  Map values = [:]
                  values[geometryFieldName] = pt
                  values[idFieldName] = i
                  f.set(values)
                  writer.add(f)
              }
          }
          catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
          "Done!"
      }
    }

    @CliCommand(value = "layer grid rowcol", help = "Create a grid Layer with rows and columns")
    String gridRowColumn(
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "rows", mandatory = true, help = "The number of rows") int rows,
            @CliOption(key = "columns", mandatory = true, help = "The number of columns") int columns,
            @CliOption(key = "geometry", mandatory = true, help = "The constraining geometry") String geometry,
            @CliOption(key = "type", specifiedDefaultValue = "polygon", unspecifiedDefaultValue = "polygon", mandatory = false, help = "The geometry type (point or polygon") String type,
            @CliOption(key = "projection", specifiedDefaultValue = "EPSG:4326", unspecifiedDefaultValue = "EPSG:4326", mandatory = false, help = "The projection") String projection,
            @CliOption(key = "geometry-field", specifiedDefaultValue = "the_geom", unspecifiedDefaultValue = "the_geom", mandatory = false, help = "The geometry field name") String geometryFieldName
    ) throws Exception {
        Workspace outputWorkspace = catalog.workspaces[workspaceName]
        if (outputWorkspace) {
            Schema schema = new Schema(outputLayerName, [
                    new Field(geometryFieldName, type.equalsIgnoreCase("point") ? "POINT" : "POLYGON", projection),
                    new Field("id", "int"),
                    new Field("row", "int"),
                    new Field("col", "int"),
                    new Field("col_row", "String")
            ])
            Bounds bounds = Geometry.fromString(geometry).bounds
            Layer outputLayer = outputWorkspace.create(schema)
            int id = 0
            outputLayer.withWriter { geoscript.layer.Writer w ->
                bounds.generateGrid(columns, rows, type, { cell, col, row ->
                    w.add(outputLayer.schema.feature([
                            "the_geom": cell,
                            "id"      : id,
                            "col"     : col,
                            "row"     : row,
                            "col_row" : "${col}_${row}"
                    ]))
                    id++
                })
            }
            catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
            "Done!"
        }
    }

    @CliCommand(value = "layer grid widthheight", help = "Create a grid Layer with cell width and height")
    String gridWidthHeight(
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "cell-width", mandatory = true, help = "The width of each cell") double cellWidth,
            @CliOption(key = "cell-height", mandatory = true, help = "The height of each cell") double cellHeight,
            @CliOption(key = "geometry", mandatory = true, help = "The constraining geometry") String geometry,
            @CliOption(key = "type", specifiedDefaultValue = "polygon", unspecifiedDefaultValue = "polygon", mandatory = false, help = "The geometry type (point or polygon") String type,
            @CliOption(key = "projection", specifiedDefaultValue = "EPSG:4326", unspecifiedDefaultValue = "EPSG:4326", mandatory = false, help = "The projection") String projection,
            @CliOption(key = "geometry-field", specifiedDefaultValue = "the_geom", unspecifiedDefaultValue = "the_geom", mandatory = false, help = "The geometry field name") String geometryFieldName
    ) throws Exception {
        Workspace outputWorkspace = catalog.workspaces[workspaceName]
        if (outputWorkspace) {
            Schema schema = new Schema(outputLayerName, [
                    new Field(geometryFieldName, type.equalsIgnoreCase("point") ? "POINT" : "POLYGON", projection),
                    new Field("id", "int"),
                    new Field("row", "int"),
                    new Field("col", "int"),
                    new Field("col_row", "String")
            ])
            Bounds bounds = Geometry.fromString(geometry).bounds
            Layer outputLayer = outputWorkspace.create(schema)
            int id = 0
            outputLayer.withWriter { geoscript.layer.Writer w ->
                bounds.generateGrid(cellWidth, cellHeight, type, { cell, col, row ->
                    w.add(outputLayer.schema.feature([
                            "the_geom": cell,
                            "id"      : id,
                            "col"     : col,
                            "row"     : row,
                            "col_row" : "${col}_${row}"
                    ]))
                    id++
                })
            }
            catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
            "Done!"
        }
    }

    @CliCommand(value = "layer copy", help = "Copy one Layer to another Workspace.")
    String copy(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "filter", mandatory = false, help = "The CQL Filter")String filter,
            @CliOption(key = "sort", mandatory = false, help = "A Sort parameter (fld dir)")String sort,
            @CliOption(key = "start", mandatory = false, unspecifiedDefaultValue = "-1", help = "The start index")int start,
            @CliOption(key = "max", mandatory = false, unspecifiedDefaultValue = "-1", help = "The maximum number of records")int max,
            @CliOption(key = "field", mandatory = false, help = "A subfield to include")String fields
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                List outputFields = inputLayer.schema.fields
                List fieldList = fields?.split(",")
                if (fieldList) {
                    outputFields = [inputLayer.schema.geom]
                    fieldList.each { name ->
                        if (inputLayer.schema.has(name) && !outputFields.find { Field fld -> fld.name.equalsIgnoreCase(name) }) {
                            outputFields.add(inputLayer.schema.get(name))
                        }
                    }
                }
                Schema schema = new Schema(outputLayerName, outputFields)
                Layer outputLayer = outputWorkspace.create(schema)
                List sortList = sort ? sort.split(",") : []
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.getCursor([filter: filter, sort: sortList, start: start, max: max, fields: fieldList]).each { f ->
                        w.add(f)
                    }
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer extent", help = "Calculate the extent of the input Layer and save it to the output Layer.")
    String extent(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "geometry-field", specifiedDefaultValue = "the_geom", unspecifiedDefaultValue = "the_geom", mandatory = false, help = "The geometry field name") String geometryFieldName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = new Schema(outputLayerName, [new Field(geometryFieldName, "Polygon", inputLayer.schema.proj)])
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.add([inputLayer.bounds.geometry])
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer extents", help = "Calculate the extents of each Feature in the input Layer and save them to the output Layer.")
    String extents(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.changeGeometryType("Polygon", outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        Map values = [:]
                        f.attributes.each { k, v ->
                            if (v instanceof geoscript.geom.Geometry) {
                                values[k] = v.bounds.geometry
                            } else {
                                values[k] = v
                            }
                        }
                        w.add(outputLayer.schema.feature(values, f.id))
                    }
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer convexhull", help = "Calculate the convexhull of the input Layer and save it to the output Layer.")
    String convexhull(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "geometry-field", specifiedDefaultValue = "the_geom", unspecifiedDefaultValue = "the_geom", mandatory = false, help = "The geometry field name") String geometryFieldName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = new Schema(outputLayerName, [new Field(geometryFieldName, "Polygon", inputLayer.schema.proj)])
                Layer outputLayer = outputWorkspace.create(schema)
                Geometry geom = new GeometryCollection(inputLayer.collectFromFeature {f ->
                    f.geom
                })
                outputLayer.add([geom.convexHull])
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer convexhulls", help = "Calculate the convexhull of each Feature in the input Layer and save them to the output Layer.")
    String convexhulls(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.changeGeometryType("Polygon", outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        Map values = [:]
                        f.attributes.each { k, v ->
                            if (v instanceof geoscript.geom.Geometry) {
                                values[k] = v.convexHull
                            } else {
                                values[k] = v
                            }
                        }
                        w.add(outputLayer.schema.feature(values, f.id))
                    }
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer voronoi", help = "Calculate a voronoi diagram of the input Layer and save it to the output Layer.")
    String voronoi(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "geometry-field", specifiedDefaultValue = "the_geom", unspecifiedDefaultValue = "the_geom", mandatory = false, help = "The geometry field name") String geometryFieldName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = new Schema(outputLayerName, [new Field(geometryFieldName, "Polygon", inputLayer.schema.proj)])
                Layer outputLayer = outputWorkspace.create(schema)
                Geometry geom = new GeometryCollection(inputLayer.collectFromFeature {f ->
                    f.geom
                }).voronoiDiagram
                geom.geometries.each { Geometry g ->
                    outputLayer.add([g])
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer coordinates", help = "Extract the coordinates each Feature in the input Layer and save them to the output Layer.")
    String coordinates(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.changeGeometryType("Point", outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        f.geom.coordinates.each { coord ->
                            Map values = [:]
                            f.attributes.each { k, v ->
                                if (v instanceof geoscript.geom.Geometry) {
                                    values[k] = new Point(coord.x, coord.y)
                                } else {
                                    values[k] = v
                                }
                            }
                            w.add(outputLayer.schema.feature(values))
                        }
                    }
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer mincircle", help = "Calculate the mininmum bounding circle of the input Layer and save it to the output Layer.")
    String mincircle(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "geometry-field", specifiedDefaultValue = "the_geom", unspecifiedDefaultValue = "the_geom", mandatory = false, help = "The geometry field name") String geometryFieldName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = new Schema(outputLayerName, [new Field(geometryFieldName, "Polygon", inputLayer.schema.proj)])
                Layer outputLayer = outputWorkspace.create(schema)
                Geometry geom = new GeometryCollection(inputLayer.collectFromFeature {f ->
                    f.geom
                })
                outputLayer.add([geom.minimumBoundingCircle])
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer mincircles", help = "Calculate the minimum bounding circle of each Feature in the input Layer and save them to the output Layer.")
    String mincircles(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.changeGeometryType("Polygon", outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        Map values = [:]
                        f.attributes.each { k, v ->
                            if (v instanceof geoscript.geom.Geometry) {
                                values[k] = v.minimumBoundingCircle
                            } else {
                                values[k] = v
                            }
                        }
                        w.add(outputLayer.schema.feature(values, f.id))
                    }
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }
}
