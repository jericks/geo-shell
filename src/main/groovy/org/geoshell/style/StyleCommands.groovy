package org.geoshell.style

import geoscript.filter.Color
import geoscript.layer.Layer
import geoscript.layer.Raster
import geoscript.style.Gradient
import geoscript.style.RasterSymbolizer
import geoscript.style.Style
import geoscript.style.Symbolizer
import geoscript.style.UniqueValues
import geoscript.style.io.SLDWriter
import geoscript.style.io.SimpleStyleReader
import geoscript.style.io.Writer
import geoscript.style.io.YSLDWriter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.geoshell.Catalog
import org.geoshell.raster.RasterName
import org.geoshell.tile.TileName
import org.geoshell.vector.LayerName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.stereotype.Component

@Component
class StyleCommands implements CommandMarker {

    @Autowired
    Catalog catalog

    @CliCommand(value = "style create", help = "Create a simple style.")
    String create(
            @CliOption(key = "params", mandatory = true, help = "The style parameters") String params,
            @CliOption(key = "file", mandatory = true, help = "The output file") File file
    ) throws Exception {
        Style style = new SimpleStyleReader().read(params)
        String type = FilenameUtils.getExtension(file.name)
        Writer styleWriter = type.equalsIgnoreCase("ysld") || type.equalsIgnoreCase("yml") ? new YSLDWriter() :new SLDWriter()
        String styleStr = styleWriter.write(style)
        file.write(styleStr)
        "Style ${params} written to ${file}!"
    }

    @CliCommand(value = "style vector default", help = "Create a default vector style.")
    String createDefaultVectorStyle(
            @CliOption(key = "layer", mandatory = true, help = "The Layer") LayerName layerName,
            @CliOption(key = "color",   mandatory = false, help = "The color",   unspecifiedDefaultValue = "#f2f2f2", specifiedDefaultValue = "#f2f2f2") String color,
            @CliOption(key = "opacity", mandatory = false, help = "The opacity", unspecifiedDefaultValue = "1.0",     specifiedDefaultValue = "1.0") double opacity,
            @CliOption(key = "file", mandatory = true, help = "The output file") File file
    ) throws Exception {
        Layer layer = catalog.layers[layerName]
        if (layer) {
            Symbolizer symbol = Symbolizer.getDefault([
                color: new Color(color),
                opacity: opacity
            ], layer.schema.geom.typ)
            String type = FilenameUtils.getExtension(file.name)
            Writer styleWriter = type.equalsIgnoreCase("ysld") || type.equalsIgnoreCase("yml") ? new YSLDWriter() :new SLDWriter()
            String styleStr = styleWriter.write(symbol)
            file.write(styleStr)
            "Default Vector Style for ${layerName} written to ${file}!"
        } else {
            "Unable to find Layer ${layerName}"
        }
    }

    @CliCommand(value = "style vector uniquevalues", help = "Create a unique values vector style.")
    String createUniqueValuesVectorStyle(
            @CliOption(key = "layer", mandatory = true, help = "The Layer") LayerName layerName,
            @CliOption(key = "field", mandatory = true, help = "The field") String field,
            @CliOption(key = "colors",   mandatory = true, help = "The colors") String colorStr,
            @CliOption(key = "file", mandatory = true, help = "The output file") File file
    ) throws Exception {
        Layer layer = catalog.layers[layerName]
        if (layer) {
            def colors
            if (colorStr) {
                def colorList = colorStr.split(" ")
                if (colorList.length > 1) {
                    colors = []
                    colors.addAll(colorList)
                }
                else if (colorStr.equalsIgnoreCase("random")) {
                    colors = { index, value -> Color.getRandomPastel() }
                }
                else {
                    colors = colorStr as String
                }
            }
            UniqueValues symbol = new UniqueValues(layer, field, colors)
            String type = FilenameUtils.getExtension(file.name)
            Writer styleWriter = type.equalsIgnoreCase("ysld") || type.equalsIgnoreCase("yml") ? new YSLDWriter() :new SLDWriter()
            String styleStr = styleWriter.write(symbol)
            file.write(styleStr)
            "Unique Values Vector Style for ${layerName}'s ${field} Field written to ${file}!"
        } else {
            "Unable to find Layer ${layerName}"
        }
    }

    @CliCommand(value = "style vector gradient", help = "Create a gradient vector style.")
    String createGradientVectorStyle(
            @CliOption(key = "layer", mandatory = true, help = "The Layer") LayerName layerName,
            @CliOption(key = "field", mandatory = true, help = "The field") String field,
            @CliOption(key = "number", mandatory = true, help = "The number of categories") int number,
            @CliOption(key = "colors", mandatory = true, help = "The colors") String colorStr,
            @CliOption(key = "method", mandatory = false, unspecifiedDefaultValue = "Quantile", specifiedDefaultValue = "Quantile", help = "The classification method (Quantile or EqualInterval)") String method,
            @CliOption(key = "elsemode", mandatory = false, unspecifiedDefaultValue = "ignore", specifiedDefaultValue = "ignore", help = "The else mode (ignore, min, max)") String elseMode,
            @CliOption(key = "file", mandatory = true, help = "The output file") File file
    ) throws Exception {
        Layer layer = catalog.layers[layerName]
        if (layer) {
            def colors
            if (colorStr) {
                def colorList = colorStr.split(" ")
                if (colorList.length > 1) {
                    colors = []
                    colors.addAll(colorList)
                }
                else if (colorStr.equalsIgnoreCase("random")) {
                    colors = { index, value -> Color.getRandomPastel() }
                }
                else {
                    colors = colorStr as String
                }
            }
            Gradient symbol = new Gradient(layer, field, method, number, colors, elseMode)
            String type = FilenameUtils.getExtension(file.name)
            Writer styleWriter = type.equalsIgnoreCase("ysld") || type.equalsIgnoreCase("yml") ? new YSLDWriter() :new SLDWriter()
            String styleStr = styleWriter.write(symbol)
            file.write(styleStr)
            "Gradient Vector Style for ${layerName}'s ${field} Field written to ${file}!"
        } else {
            "Unable to find Layer ${layerName}"
        }
    }

    @CliCommand(value = "style raster default", help = "Create a default raster style.")
    String createDefaultRasterStyle(
            @CliOption(key = "raster", mandatory = true, help = "The Raster") RasterName rasterName,
            @CliOption(key = "opacity", mandatory = false, help = "The opacity", unspecifiedDefaultValue = "1.0", specifiedDefaultValue = "1.0") double opacity,
            @CliOption(key = "file", mandatory = true, help = "The output file") File file
    ) throws Exception {
        Raster raster = catalog.rasters[rasterName]
        if (raster) {
            Symbolizer symbol = new RasterSymbolizer(opacity)
            String type = FilenameUtils.getExtension(file.name)
            Writer styleWriter = type.equalsIgnoreCase("ysld") || type.equalsIgnoreCase("yml") ? new YSLDWriter() :new SLDWriter()
            String styleStr = styleWriter.write(symbol)
            file.write(styleStr)
            "Default Raster Style for ${rasterName} written to ${file}!"
        } else {
            "Unable to find Raster ${rasterName}"
        }
    }

}
