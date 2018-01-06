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
import geoscript.proj.Projection
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

    @CliCommand(value = "layer projection", help = "Get the Projection of a Layer.")
    String projection(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName name
    ) throws Exception {
        Layer layer = catalog.layers[name]
        if (layer) {
            "${layer.proj.id}"
        } else {
            "Unable to find Layer ${name}"
        }
    }

    @CliCommand(value = "layer features", help = "Display the Features of a Layer.")
    String features(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName layerName,
            @CliOption(key = "filter", mandatory = false, help = "The CQL Filter")String filter,
            @CliOption(key = "sort", mandatory = false, help = "A Sort parameter (fld dir)")String sort,
            @CliOption(key = "start", mandatory = false, unspecifiedDefaultValue = "-1", help = "The start index")int start,
            @CliOption(key = "max", mandatory = false, unspecifiedDefaultValue = "-1", help = "The maximum number of records")int max,
            @CliOption(key = "field", mandatory = false, help = "A subfield to include") String fields
    ) throws Exception {
        Layer layer = catalog.layers[layerName]
        if (layer) {
            StringBuilder str = new StringBuilder()
            List fieldList = fields ? fields.split(",") as List : layer.schema.fields.collect { it.name }
            List sortList = sort ? sort.split(",") as List : []
            // If max is set but start isn't start at 0
            if (max > -1 && start == -1) {
                start = 0
            }
            // If start is set but max isn't, set max
            else if (start > -1 && max == -1) {
                max = layer.count - start
            }
            str.append(OsUtils.LINE_SEPARATOR)
            layer.getCursor([filter: filter, sort: sortList, start: start, max: max, fields: fieldList]).each { Feature f ->
                String header = "Feature (${f.id})"
                str.append(header).append(OsUtils.LINE_SEPARATOR)
                str.append("-".multiply(header.length())).append(OsUtils.LINE_SEPARATOR)
                fieldList.each { String fld ->
                    str.append(fld).append(" = ").append(f[fld]).append(OsUtils.LINE_SEPARATOR)
                }
                str.append(OsUtils.LINE_SEPARATOR)
            }
            str
        } else {
            "Unable to find Layer ${layerName}"
        }
    }

    @CliCommand(value = "layer validity", help = "Check for invalid geometries in the Layer.")
    String validity(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName name,
            @CliOption(key = "fields", mandatory = false, help = "A comma delimited list of Fields to include") String fields
    ) throws Exception {
        Layer layer = catalog.layers[name]
        if (layer) {
            List invalidGeometries = []
            layer.eachFeature { Feature f ->
                if (!f.geom.valid) {
                    String reason = f.geom.validReason
                    String values = fields ? fields.split(",").collect { f.get(it) }.join(",") : f.id
                    invalidGeometries.add([reason: reason, values: values])
                }
            }
            if (invalidGeometries.size() > 0) {
                Table table = new Table()
                table.addHeader(0, new TableHeader("Values", 20))
                table.addHeader(1, new TableHeader("Reason", 20))
                invalidGeometries.each { Map invalid ->
                    TableRow row = table.newRow()
                    row.addValue(0, invalid.values)
                    row.addValue(1, invalid.reason)
                }
                table.calculateColumnWidths()
                table.toString()
            } else {
                "No invalid geometries!"
            }
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

    @CliCommand(value = "layer create", help = "Create a new Layer.")
    String create(
            @CliOption(key = "workspace", mandatory = true, help = "The Workspace name") WorkspaceName workspaceName,
            @CliOption(key = "name", mandatory = true, help = "The new Layer name") String name,
            @CliOption(key = "fields", mandatory = true, help = "The pipe delimited list of fields (name=type)") String fieldStr
    ) throws Exception {
        Workspace workspace = catalog.workspaces[workspaceName]
        if (workspace) {
            // Get Fields
            List<Field> fields = []
            fieldStr.split("\\|").each { String f ->
                Field field
                List<String> nameType = f.split("=")
                String fieldName = nameType[0]
                String fieldType = nameType[1]
                if (fieldType.contains("EPSG")) {
                    List<String> parts = fieldType.split(" ")
                    field = new Field(fieldName, parts[0], parts[1].startsWith("EPSG") ? parts[1] : "EPSG:${parts[1]}")
                } else {
                    field = new Field(fieldName, fieldType)
                }
                fields.add(field)
            }
            // Create Schema
            Schema schema = new Schema(name, fields)
            // Create Layer
            Layer layer = workspace.create(schema)
            // Add Layer to Catalog
            catalog.layers[new LayerName(name)] = layer
            "Created Layer ${name}!"
        } else {
            "Unable to find Workspace ${workspaceName}"
        }
    }

    @CliCommand(value = "layer add", help = "Add a new Feature to a Layer.")
    String add(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName layerName,
            @CliOption(key = "values", mandatory = true, help = "The pipe delimited list of values (field=value)") String valueStr
    ) throws Exception {
        Layer layer = catalog.layers[layerName]
        if (layer) {
            Map values = [:]
            valueStr.split("\\|").each { String str ->
                List parts = str.split("=")
                values[parts[0]] = parts[1]
            }
            Feature feature = layer.schema.feature(values)
            layer.add(feature)
            "Added Feature to ${layerName}"
        } else {
            "Unable to find Layer ${layerName}"
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

    @CliCommand(value = "layer delaunay", help = "Calculate a delaunay diagram of the input Layer and save it to the output Layer.")
    String delaunay(
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
                }).delaunayTriangleDiagram
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

    @CliCommand(value = "layer octagonalenvelope", help = "Calculate the octagonal envelope of the input Layer and save it to the output Layer.")
    String octagonalenvelope(
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
                outputLayer.add([geom.octagonalEnvelope])
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer octagonalenvelopes", help = "Calculate the octagonal envelope of each Feature in the input Layer and save them to the output Layer.")
    String octagonalenvelopes(
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
                                values[k] = v.octagonalEnvelope
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

    @CliCommand(value = "layer minrect", help = "Calculate the mininmum rectangle of the input Layer and save it to the output Layer.")
    String minrect(
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
                outputLayer.add([geom.minimumRectangle])
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer minrects", help = "Calculate the minimum rectangle of each Feature in the input Layer and save them to the output Layer.")
    String minrects(
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
                                values[k] = v.minimumRectangle
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

    @CliCommand(value = "layer addareafield", help = "Add area Field to the input Layer and save the result to the output Layer")
    String addareafield(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "area-fieldname", mandatory = true, specifiedDefaultValue = "area", unspecifiedDefaultValue = "area", help = "The area field name") String areaFieldName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.addField(new Field(areaFieldName,"Double"), outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        Map attributes = f.attributes
                        attributes[areaFieldName] = f.geom.area
                        w.add(outputLayer.schema.feature(attributes, f.id))
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

    @CliCommand(value = "layer addidfield", help = "Add area ID to the input Layer and save the result to the output Layer")
    String addidfield(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "id-fieldname", mandatory = true, specifiedDefaultValue = "id", unspecifiedDefaultValue = "id", help = "The id field name") String idFieldName,
            @CliOption(key = "start-value", mandatory = true, specifiedDefaultValue = "1", unspecifiedDefaultValue = "1", help = "The value to start at") int start
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.addField(new Field(idFieldName,"int"), outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                int c = start
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        Map attributes = f.attributes
                        attributes[idFieldName] = c
                        w.add(outputLayer.schema.feature(attributes, f.id))
                        c++
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

    @CliCommand(value = "layer addxyfields", help = "Add x and y coordinate Fields to the input Layer and save the result to the output Layer")
    String addxyfields(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "x-fieldname", mandatory = true, specifiedDefaultValue = "x", unspecifiedDefaultValue = "x", help = "The x field name") String xFieldName,
            @CliOption(key = "y-fieldname", mandatory = true, specifiedDefaultValue = "y", unspecifiedDefaultValue = "y", help = "The y field name") String yFieldName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.addFields([
                    new Field(xFieldName, "double"),
                    new Field(yFieldName, "double")
                ], outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        Map attributes = f.attributes
                        Point pt = f.geom.centroid
                        attributes[xFieldName] = pt.x
                        attributes[yFieldName] = pt.y
                        w.add(outputLayer.schema.feature(attributes, f.id))
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

    @CliCommand(value = "layer addfields", help = "Add Fields to the input Layer and save the result to the output Layer")
    String addfields(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "fields", mandatory = true, help = "The Fields (name=type proj)") String fields
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.addFields(fields.split(",").collect { String fld ->
                    Field field
                    List fldParts = fld.split("=")
                    String key = fldParts[0]
                    String value = fldParts[1]
                    if (value.contains("EPSG")) {
                        def parts = value.split(" ")
                        field = new Field(key, parts[0], parts[1].startsWith("EPSG") ? parts[1] : "EPSG:${parts[1]}")
                    } else {
                        field = new Field(key, value)
                    }
                    field
                }, outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
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

    @CliCommand(value = "layer removefields", help = "Remove Fields to the input Layer and save the result to the output Layer")
    String removefields(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "fields", mandatory = true, help = "The Fields (name=type proj)") String fields
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = inputLayer.schema.removeFields(fields.split(",").collect { String fldName ->
                    inputLayer.schema.get(fldName)
                }, outputLayerName)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
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

    @CliCommand(value = "layer simplify", help = "Simplify the features of the input Layer and save them to the output Layer")
    String simplify(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "algorithm", mandatory = false, specifiedDefaultValue = "tp", unspecifiedDefaultValue = "tp", help = "The simplify algorithm (DouglasPeucker - dp or TopologyPreserving - tp)") String algorithm,
            @CliOption(key = "distance", mandatory = true, help = "The distance tolerance") double distance
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = new Schema(outputLayerName, inputLayer.schema.fields)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        Map values = [:]
                        f.attributes.each { k, v ->
                            if (v instanceof geoscript.geom.Geometry) {
                                Geometry geometry = f.geom
                                if (algorithm.equalsIgnoreCase("douglaspeucker") || algorithm.equalsIgnoreCase("dp")) {
                                    geometry = geometry.simplify(distance)
                                } else /*if (algorithm.equalsIgnoreCase("topologypreserving") || algorithm.equalsIgnoreCase("tp"))*/ {
                                    geometry = geometry.simplifyPreservingTopology(distance)
                                }
                                values[k] = geometry
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

    @CliCommand(value = "layer densify", help = "Densify the features of the input Layer and save them to the output Layer")
    String densify(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "distance", mandatory = true, help = "The distance tolerance") double distance
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Schema schema = new Schema(outputLayerName, inputLayer.schema.fields)
                Layer outputLayer = outputWorkspace.create(schema)
                outputLayer.withWriter { geoscript.layer.Writer w ->
                    inputLayer.eachFeature { Feature f ->
                        Map values = [:]
                        f.attributes.each { k, v ->
                            if (v instanceof geoscript.geom.Geometry) {
                                values[k] = f.geom.densify(distance)
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

    @CliCommand(value = "layer delete", help = "Delete features from the Layer")
    String delete(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName layerName,
            @CliOption(key = "filter", mandatory = true, help = "The CQL Filter") String cql
    ) throws Exception {
        Layer layer = catalog.layers[layerName]
        if (layer) {
            layer.delete(cql)
            "Deleted ${cql} Features from ${layerName}"
        } else {
            "Unable to find Layer ${layerName}"
        }
    }

    @CliCommand(value = "layer updatefield", help = "Delete features from the Layer")
    String updatefield(
            @CliOption(key = "name", mandatory = true, help = "The Layer name") LayerName layerName,
            @CliOption(key = "field", mandatory = true, help = "The field name") String fieldName,
            @CliOption(key = "value", mandatory = true, help = "The value") String value,
            @CliOption(key = "filter", mandatory = false, unspecifiedDefaultValue = "INCLUDE", specifiedDefaultValue = "INCLUDE", help = "The CQL Filter") String filter,
            @CliOption(key = "script", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "false", help = "Whether the value is a script or not") Boolean script
    ) throws Exception {
        Layer layer = catalog.layers[layerName]
        if (layer) {
            Field field = layer.schema.get(fieldName)
            layer.update(field, value, filter, script)
            "Done updating ${fieldName} with ${value}!"
        } else {
            "Unable to find Layer ${layerName}"
        }
    }

    @CliCommand(value = "layer transform", help = "Transform the features of the input Layer and save them to the output Layer")
    String transform(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "transforms", mandatory = true, help = "The pipe delimited list of transforms (field=expression or function)") String transforms
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Map transformMap = [:]
                transforms.split("\\|").each { String transform ->
                    List parts = transform.split("=")
                    transformMap[parts[0]] = parts[1]
                }
                Layer layer = inputLayer.transform(outputLayerName, transformMap)
                Layer outputLayer = outputWorkspace.add(layer)
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done transforming ${inputLayerName} to ${outputLayerName} with ${transforms}!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer reproject", help = "Transform the features of the input Layer and save them to the output Layer")
    String reproject(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "projection", mandatory = true, help = "The projection") String projection
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Layer outputLayer = inputLayer.reproject(new Projection(projection), outputWorkspace, outputLayerName)
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done reprojecting ${inputLayerName} to ${outputLayerName} in ${projection}!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer clip", help = "Clip the input Layer by the other Layer to produce the output Layer")
    String clip(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "clip-name", mandatory = true, help = "The clip Layer name") LayerName clipLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Layer clipLayer = catalog.layers[clipLayerName]
            if (clipLayer) {
                Workspace outputWorkspace = catalog.workspaces[workspaceName]
                if (outputWorkspace) {
                    Layer outputLayer = inputLayer.clip(clipLayer, outLayer: outputLayerName, outWorkspace: outputWorkspace)
                    catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                    "Done clipping ${inputLayerName} to ${clipLayerName} to create ${outputLayerName}!"
                } else {
                    "Unable to find Workspace ${workspaceName}"
                }
            } else {
                "Unable to find clip Layer ${clipLayerName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer union", help = "Union a Layer with another Layer")
    String union(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "other-name", mandatory = true, help = "The other Layer name") LayerName otherLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "postfix-all", mandatory = false, specifiedDefaultValue = "false", unspecifiedDefaultValue = "false", help = "Whether to postfix all field names when combining schemas") boolean postfixAll,
            @CliOption(key = "include-duplicates", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "true", help = "Whether to include duplicate field names") boolean includeDuplicates
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Layer otherLayer = catalog.layers[otherLayerName]
            if (otherLayer) {
                Workspace outputWorkspace = catalog.workspaces[workspaceName]
                if (outputWorkspace) {
                    Layer outputLayer = inputLayer.union(otherLayer, outLayer: outputLayerName, outWorkspace: outputWorkspace, postfixAll: postfixAll, includeDuplicates: includeDuplicates)
                    catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                    "Done unioning ${inputLayerName} and ${otherLayerName} to create ${outputLayerName}!"
                } else {
                    "Unable to find Workspace ${workspaceName}"
                }
            } else {
                "Unable to find other Layer ${otherLayerName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer intersection", help = "Calculate the intersection between a Layer with another Layer")
    String intersection(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "other-name", mandatory = true, help = "The other Layer name") LayerName otherLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "postfix-all", mandatory = false, specifiedDefaultValue = "false", unspecifiedDefaultValue = "false", help = "Whether to postfix all field names when combining schemas") boolean postfixAll,
            @CliOption(key = "include-duplicates", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "true", help = "Whether to include duplicate field names") boolean includeDuplicates
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Layer otherLayer = catalog.layers[otherLayerName]
            if (otherLayer) {
                Workspace outputWorkspace = catalog.workspaces[workspaceName]
                if (outputWorkspace) {
                    Layer outputLayer = inputLayer.intersection(otherLayer, outLayer: outputLayerName, outWorkspace: outputWorkspace, postfixAll: postfixAll, includeDuplicates: includeDuplicates)
                    catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                    "Done calculating the intersection between ${inputLayerName} and ${otherLayerName} to create ${outputLayerName}!"
                } else {
                    "Unable to find Workspace ${workspaceName}"
                }
            } else {
                "Unable to find other Layer ${otherLayerName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer erase", help = "Erase one Layer from another Layer")
    String erase(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "other-name", mandatory = true, help = "The other Layer name") LayerName otherLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Layer otherLayer = catalog.layers[otherLayerName]
            if (otherLayer) {
                Workspace outputWorkspace = catalog.workspaces[workspaceName]
                if (outputWorkspace) {
                    Layer outputLayer = inputLayer.erase(otherLayer, outLayer: outputLayerName, outWorkspace: outputWorkspace)
                    catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                    "Done erasing ${inputLayerName} from ${otherLayerName} to create ${outputLayerName}!"
                } else {
                    "Unable to find Workspace ${workspaceName}"
                }
            } else {
                "Unable to find other Layer ${otherLayerName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }

    @CliCommand(value = "layer identity", help = "Calculate the intersection between a Layer with another Layer")
    String identity(
            @CliOption(key = "input-name", mandatory = true, help = "The Layer name") LayerName inputLayerName,
            @CliOption(key = "other-name", mandatory = true, help = "The other Layer name") LayerName otherLayerName,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "postfix-all", mandatory = false, specifiedDefaultValue = "false", unspecifiedDefaultValue = "false", help = "Whether to postfix all field names when combining schemas") boolean postfixAll,
            @CliOption(key = "include-duplicates", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "true", help = "Whether to include duplicate field names") boolean includeDuplicates
    ) throws Exception {
        Layer inputLayer = catalog.layers[inputLayerName]
        if (inputLayer) {
            Layer otherLayer = catalog.layers[otherLayerName]
            if (otherLayer) {
                Workspace outputWorkspace = catalog.workspaces[workspaceName]
                if (outputWorkspace) {
                    Layer outputLayer = inputLayer.identity(otherLayer, outLayer: outputLayerName, outWorkspace: outputWorkspace, postfixAll: postfixAll, includeDuplicates: includeDuplicates)
                    catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                    "Done calculating the identity between ${inputLayerName} and ${otherLayerName} to create ${outputLayerName}!"
                } else {
                    "Unable to find Workspace ${workspaceName}"
                }
            } else {
                "Unable to find other Layer ${otherLayerName}"
            }
        } else {
            "Unable to find Layer ${inputLayerName}"
        }
    }
}
