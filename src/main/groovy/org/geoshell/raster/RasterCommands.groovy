package org.geoshell.raster

import geoscript.feature.Feature
import geoscript.feature.Field
import geoscript.feature.Schema
import geoscript.geom.Bounds
import geoscript.geom.Geometry
import geoscript.geom.Point
import geoscript.layer.Band
import geoscript.layer.Format
import geoscript.layer.Layer
import geoscript.layer.Raster
import geoscript.proj.Projection
import geoscript.style.Style
import geoscript.style.io.CSSReader
import geoscript.style.io.SLDReader
import geoscript.style.io.SLDWriter
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.shell.support.util.OsUtils
import org.springframework.stereotype.Component

@Component
class RasterCommands implements CommandMarker {

    @Autowired
    Catalog catalog

    @CliCommand(value = "raster open", help = "Open a Raster.")
    String open(
            @CliOption(key = "format", mandatory = true, help = "The Format name") FormatName formatName,
            @CliOption(key = "raster", mandatory = true, help = "The Raster name") RasterName rasterName,
            @CliOption(key = "name", mandatory = false, help = "The name") String name
    ) throws Exception {
        if (!name) {
            name = "${formatName.name}:${rasterName.name}"
        }
        Format format = catalog.formats[formatName]
        if (format) {
            if (format.names.contains(rasterName.name)) {
                Raster raster = format.read(rasterName.name)
                catalog.rasters[new RasterName(name ?: rasterName.name)] = raster
                "Opened Format ${formatName.name} Raster ${rasterName.name} as ${name}"
            } else {
                "Unable to find Raster ${rasterName}"
            }
        } else {
            "Unable to find Format ${formatName}"
        }
    }

    @CliCommand(value = "raster close", help = "Close a Raster.")
    String close(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            raster.dispose()
            catalog.rasters.remove(name)
            "Raster ${name} closed!"
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster list", help = "List open Rasters.")
    String list() throws Exception {
        catalog.rasters.collect { RasterName name, Raster raster ->
            "${name} = ${raster.format}"
        }.join(OsUtils.LINE_SEPARATOR)
    }

    @CliCommand(value = "raster info", help = "Get information about a Raster.")
    String info(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            String NEW_LINE = System.getProperty("line.separator")
            StringBuilder builder = new StringBuilder()
            builder.append("Format: ${raster.format ? raster.format.name : 'Unknown'}")
            builder.append(NEW_LINE)
            builder.append("Size: ${raster.size[0]}, ${raster.size[1]}")
            builder.append(NEW_LINE)
            builder.append("Projection ID: ${raster.proj != null ? raster.proj.id : 'Unknown'}")
            builder.append(NEW_LINE)
            builder.append("Projection WKT: ${raster.proj != null ? raster.proj.wkt : 'Unknown'}")
            builder.append(NEW_LINE)
            builder.append("Extent: ${raster.bounds.minX}, ${raster.bounds.minY}, ${raster.bounds.maxX}, ${raster.bounds.maxY}")
            builder.append(NEW_LINE)
            builder.append("Pixel Size: ${raster.pixelSize[0]}, ${raster.pixelSize[1]}")
            builder.append(NEW_LINE)
            builder.append("Block Size: ${raster.blockSize[0]}, ${raster.blockSize[1]}")
            builder.append(NEW_LINE)
            Map extrema = raster.extrema
            builder.append("Bands:")
            raster.bands.eachWithIndex { Band b, int i ->
                builder.append(NEW_LINE)
                builder.append("   ${b}")
                builder.append(NEW_LINE)
                builder.append("      Min Value: ${extrema.min[i]} Max Value: ${extrema.max[i]}")
            }
            builder.toString()
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster value", help = "Get a value from the Raster.")
    String value(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "band", mandatory = false, unspecifiedDefaultValue = "0", specifiedDefaultValue = "0", help = "The x coordinate") int band,
            @CliOption(key = "x", mandatory = true, help = "The x coordinate") double x,
            @CliOption(key = "y", mandatory = true, help = "The y coordinate") double y,
            @CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "geometry", specifiedDefaultValue = "geometry", help = "The y coordinate") String type
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Object value
            if (type.equalsIgnoreCase("geometry")) {
                value = raster.getValue(new Point(x,y), band)
            } else {
                value = raster.getValue(x as int, y as int, band)
            }
            "${value}"
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster crop", help = "Crop a Raster.")
    String crop(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName,
            @CliOption(key = "geometry", mandatory = true, help = "The geometry") String geometry
            ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster croppedRaster = raster.crop(Geometry.fromString(geometry))
                format.write(croppedRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Raster ${name} cropped to ${outputRasterName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster reproject", help = "Project a Raster.")
    String reproject(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName,
            @CliOption(key = "projection", mandatory = true, help = "The projection") String projection
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster reprojectedRaster = raster.reproject(new Projection(projection))
                format.write(reprojectedRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Raster ${name} reprojected to ${outputRasterName} as ${projection}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster style set", help = "Set a Raster's style")
    String setStyle(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "style", mandatory = true, help = "The SLD or CSS File") File styleFile
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Style style = null
            if (styleFile.name.endsWith(".sld")) {
                style = new SLDReader().read(styleFile)
            } else if (styleFile.name.endsWith(".css")) {
                style = new CSSReader().read(styleFile)
            }
            if (style) {
                raster.style = style
                "Style ${styleFile.absolutePath} set on ${name}"
            } else {
                "Unable to read ${styleFile.absolutePath}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster style get", help = "Get the Raster's style.")
    String getStyle(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "style", mandatory = false, help = "The SLD File") File styleFile
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            if (styleFile) {
                new SLDWriter().write(raster.style, styleFile)
                "${name} style written to ${styleFile}"
            } else {
                new SLDWriter().write(raster.style)
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster contours", help = "Create contours.")
    String contours(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName,
            @CliOption(key = "band", mandatory = false, unspecifiedDefaultValue = "0", specifiedDefaultValue = "0", help = "The Raster band to contour") int band,
            @CliOption(key = "levels", mandatory = true, help = "The contour level or interval") String levels,
            @CliOption(key = "simplify", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "false", help = "Whether to simplify") boolean simplify,
            @CliOption(key = "smooth", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "false", help = "Whether to smooth") boolean smooth,
            @CliOption(key = "bounds", mandatory = false, help = "The Bounds") String bounds
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Layer contourLayer = raster.contours(
                    band,
                    levels.split(",").collect { Double.parseDouble(it) },
                    simplify,
                    smooth,
                    bounds ? Bounds.fromString(bounds) : raster.bounds
                )
                Layer layer = outputWorkspace.create(new Schema(outputLayerName, [
                    new Field("the_geom", "LineString"), new Field("value", "double")
                ]))
                layer.withWriter { geoscript.layer.Writer w ->
                    contourLayer.cursor.each { Feature f ->
                        w.add(f)
                    }
                }
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done creating contours!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster envelope", help = "Create a Vector Layer from the envelope of a Raster.")
    String envelope(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-workspace", mandatory = true, help = "The output Layer Workspace") WorkspaceName workspaceName,
            @CliOption(key = "output-name", mandatory = true, help = "The output Layer name") String outputLayerName
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Workspace outputWorkspace = catalog.workspaces[workspaceName]
            if (outputWorkspace) {
                Layer layer = outputWorkspace.create(new Schema(outputLayerName, [
                        new Field("the_geom", "Polygon")
                ]))
                layer.add([the_geom: raster.bounds.geometry])
                catalog.layers[new LayerName(outputLayerName)] = outputWorkspace.get(outputLayerName)
                "Done creating envelope in ${outputLayerName} from ${name}!"
            } else {
                "Unable to find Workspace ${workspaceName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster reclassify", help = "Reclassify a Raster.")
    String reclassify(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName,
            @CliOption(key = "ranges", mandatory = true, help = "The comma delimited reclassification ranges (from-to=value)") String ranges,
            @CliOption(key = "band", mandatory = false, unspecifiedDefaultValue = "0", specifiedDefaultValue = "0", help = "The Raster band to contour") int band,
            @CliOption(key = "nodata", mandatory = false, unspecifiedDefaultValue = "0", specifiedDefaultValue = "0", help = "The NODATA value") double noData
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster reclassifiedRaster = raster.reclassify(ranges.split(",").collect { String range ->
                    int dash = range.indexOf("-")
                    int equal = range.indexOf("=")
                    def from = range.substring(0, dash).trim() as int
                    def to = range.substring(dash + 1, equal).trim() as int
                    def value = range.substring(equal + 1).trim() as int
                    [min: from, max: to, value: value]
                }, band: band, noData: noData)
                format.write(reclassifiedRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Raster ${name} reclassified to ${outputRasterName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster scale", help = "Scale a Raster.")
    String scale(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName,
            @CliOption(key = "x", mandatory = true, help = "The scale factor along the x axis") float x,
            @CliOption(key = "y", mandatory = true, help = "The scale factor along the y axis") float y,
            @CliOption(key = "x-trans", mandatory = false, unspecifiedDefaultValue = "0", specifiedDefaultValue = "0", help = "The x translation") float xTrans,
            @CliOption(key = "y-trans", mandatory = false, unspecifiedDefaultValue = "0", specifiedDefaultValue = "0", help = "The y translation") float yTrans,
            @CliOption(key = "interpolation", mandatory = false, unspecifiedDefaultValue = "nearest", specifiedDefaultValue = "nearest", help = "The interpolation method (bicubic, bicubic2, bilinear, nearest)") String interpolation
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster scaledRaster = raster.scale(x, y, xTrans, yTrans, interpolation)
                format.write(scaledRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Raster ${name} scaled to ${outputRasterName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster add constant", help = "Add constant values to a Raster")
    String addConstant(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName,
            @CliOption(key = "values", mandatory = true, help = "The values") String values
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster addRaster = raster.add(values.split(",").collect { String v -> Double.parseDouble(v)})
                format.write(addRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Added ${values} to ${name} to create ${outputRasterName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster subtract constant", help = "Subtract constant values from a Raster")
    String subtractConst(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName,
            @CliOption(key = "values", mandatory = true, help = "The values") String values,
            @CliOption(key = "from", mandatory = false, specifiedDefaultValue = "false", unspecifiedDefaultValue = "false", help = "Whether to subtract the Raster from the constant or vice verse") boolean from
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster subtractRaster
                List valueList = values.split(",").collect { String v -> Double.parseDouble(v)}
                if (from) {
                    subtractRaster = raster.minusFrom(valueList)
                } else {
                    subtractRaster = raster.minus(valueList)
                }

                format.write(subtractRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Subtracted ${from ? name : values} from ${from ? values : name} to create ${outputRasterName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster multiply constant", help = "Multiply constant values to a Raster")
    String multiplyConstant(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName,
            @CliOption(key = "values", mandatory = true, help = "The values") String values
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster multiplyRaster = raster.multiply(values.split(",").collect { String v -> Double.parseDouble(v)})
                format.write(multiplyRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Multiplied ${name} by ${values} to create ${outputRasterName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster divide constant", help = "Divide constant values against a Raster")
    String divideConstant(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName,
            @CliOption(key = "values", mandatory = true, help = "The values") String values
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster divideRaster = raster.divide(values.split(",").collect { String v -> Double.parseDouble(v)})
                format.write(divideRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Divided ${name} by ${values} to create ${outputRasterName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

    @CliCommand(value = "raster stylize", help = "Create a new Raster by baking the style into an existing Raster")
    String stylize(
            @CliOption(key = "name", mandatory = true, help = "The Raster name") RasterName name,
            @CliOption(key = "output-format", mandatory = true, help = "The output Format Workspace") FormatName formatName,
            @CliOption(key = "output-name", mandatory = false, help = "The output Raster name") String outputRasterName
    ) throws Exception {
        Raster raster = catalog.rasters[name]
        if (raster) {
            Format format = catalog.formats[formatName]
            if (format) {
                Raster stylizedRaster = raster.stylize()
                format.write(stylizedRaster)
                if (!outputRasterName) {
                    outputRasterName = formatName.name
                }
                catalog.rasters[new RasterName(outputRasterName)] = format.read(outputRasterName)
                "Stylized ${name} to create ${outputRasterName}!"
            } else {
                "Unable to find Raster Format ${formatName}"
            }
        } else {
            "Unable to find Raster ${name}"
        }
    }

}
