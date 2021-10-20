package org.geoshell.style

import geoscript.filter.Color
import geoscript.layer.Layer
import geoscript.layer.Raster
import geoscript.style.ColorMap
import geoscript.style.Gradient
import geoscript.style.RasterSymbolizer
import geoscript.style.Style
import geoscript.style.StyleRepository
import geoscript.style.Symbolizer
import geoscript.style.UniqueValues
import geoscript.style.io.SLDWriter
import geoscript.style.io.SimpleStyleReader
import geoscript.style.io.UniqueValuesReader
import geoscript.style.io.Writer
import geoscript.style.io.YSLDWriter
import org.apache.commons.io.FilenameUtils
import org.geoshell.Catalog
import org.geoshell.raster.RasterName
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

    @CliCommand(value = "style vector uniquevaluesfromtext", help = "Create a unique values vector style from a text file")
    String createUniqueValuesStyleFromText(
            @CliOption(key = "field", mandatory = true, help = "The field name") String field,
            @CliOption(key = "geometryType", mandatory = true, help = "The geometry type") String geometryType,
            @CliOption(key = "textFile", mandatory = true, help = "The input text file") File textFile,
            @CliOption(key = "styleFile", mandatory = true, help = "The output sld or ysld file") File outputFile
    ) throws Exception {
        UniqueValuesReader uniqueValuesReader = new UniqueValuesReader(field, geometryType)
        Style style = uniqueValuesReader.read(textFile)
        String type = FilenameUtils.getExtension(outputFile.name)
        Writer styleWriter = type.equalsIgnoreCase("ysld") || type.equalsIgnoreCase("yml") ? new YSLDWriter() :new SLDWriter()
        String styleStr = styleWriter.write(style)
        outputFile.write(styleStr)
        "Create a unique values style from ${textFile} for ${field} and ${geometryType} to ${outputFile}"
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

    @CliCommand(value = "style raster colormap", help = "Create a color map raster style.")
    String createColorMapRasterStyle(
            @CliOption(key = "raster", mandatory = true, help = "The Raster") RasterName rasterName,
            @CliOption(key = "opacity", mandatory = false, help = "The opacity", unspecifiedDefaultValue = "1.0", specifiedDefaultValue = "1.0") double opacity,
            @CliOption(key = "values", mandatory = true, help = "The comma delimited list of values (key=value)") String valueStr,
            @CliOption(key = "type", unspecifiedDefaultValue = "ramp", specifiedDefaultValue = "ramp", mandatory = false, help = "The type (intervals, values, ramp)") String type,
            @CliOption(key = "extended", unspecifiedDefaultValue = "false", specifiedDefaultValue = "false", mandatory = false, help = "Whether to use extended colors or not") boolean extended,
            @CliOption(key = "file", mandatory = true, help = "The output file") File file
    ) throws Exception {
        Raster raster = catalog.rasters[rasterName]
        if (raster) {
            ColorMap symbol = new ColorMap(valueStr.split(",").collect { String v ->
                List parts = v.split("=") as List
                [quantity: parts[0], color: new Color(parts[1]).hex]
            }, type, extended)
            symbol.opacity = opacity
            String fileType = FilenameUtils.getExtension(file.name)
            Writer styleWriter = fileType.equalsIgnoreCase("ysld") || type.equalsIgnoreCase("yml") ? new YSLDWriter() :new SLDWriter()
            String styleStr = styleWriter.write(symbol)
            file.write(styleStr)
            "Colormap Raster Style for ${rasterName} written to ${file}!"
        } else {
            "Unable to find Raster ${rasterName}"
        }
    }

    @CliCommand(value = "style raster palette colormap", help = "Create a color map raster style from a color palette.")
    String createColorMapPaletteRasterStyle(
            @CliOption(key = "min", mandatory = true, help = "The min value") double minValue,
            @CliOption(key = "max", mandatory = true, help = "The max value") double maxValue,
            @CliOption(key = "palette", mandatory = true, help = "The color palette name (from Color Brewer)") String palette,
            @CliOption(key = "number", mandatory = true, help = "The number of categories") int numberOfCategories,
            @CliOption(key = "type", unspecifiedDefaultValue = "ramp", specifiedDefaultValue = "ramp", mandatory = false, help = "The type of interpolation") String type,
            @CliOption(key = "extended", unspecifiedDefaultValue = "false", specifiedDefaultValue = "false", mandatory = false, help = "Whether to use extended colors") boolean extended,
            @CliOption(key = "opacity", mandatory = false, help = "The opacity", unspecifiedDefaultValue = "1.0", specifiedDefaultValue = "1.0") double opacity,
            @CliOption(key = "file", mandatory = true, help = "The output file") File file
    ) throws Exception {
        ColorMap symbol = new ColorMap(minValue, maxValue, palette, numberOfCategories, type, extended)
        symbol.opacity = opacity
        String fileType = FilenameUtils.getExtension(file.name)
        Writer styleWriter = fileType.equalsIgnoreCase("ysld") || type.equalsIgnoreCase("yml") ? new YSLDWriter() :new SLDWriter()
        String styleStr = styleWriter.write(symbol)
        file.write(styleStr)
        "Colormap Palette Raster Style written to ${file}!"
    }

    @CliCommand(value = "style repository save", help = "Save a style to a style repository")
    String saveStyleToStyleRepository(
        @CliOption(key = "type", mandatory = true, help = "The type of style repository (directory, nested-directory, h2, sqlite, postgres)")
        String type,
        @CliOption(key = "options", mandatory = true, help = "The style repository options")
        String params,
        @CliOption(key = "layerName", mandatory = true, help = "The layer name")
        String layerName,
        @CliOption(key = "styleName", mandatory = true, help = "The style name")
        String styleName,
        @CliOption(key = "styleFile", mandatory = true, help = "The style file (sld or css)")
        File styleFile
    ) {
        StyleRepository styleRepository = StyleRepositoryFactory.getStyleRepository(type, StyleRepositoryFactory.getParameters(params))
        styleRepository.save(layerName, styleName, styleFile.text)
        "Style ${styleName} for Layer ${layerName} saved to ${type}"
    }

    @CliCommand(value = "style repository delete", help = "Delete a style from a style repository")
    String deleteStyleFromStyleRepository(
            @CliOption(key = "type", mandatory = true, help = "The type of style repository (directory, nested-directory, h2, sqlite, postgres)")
            String type,
            @CliOption(key = "options", mandatory = true, help = "The style repository options")
            String params,
            @CliOption(key = "layerName", mandatory = true, help = "The layer name")
            String layerName,
            @CliOption(key = "styleName", mandatory = true, help = "The style name")
            String styleName
    ) {
        StyleRepository styleRepository = StyleRepositoryFactory.getStyleRepository(type, StyleRepositoryFactory.getParameters(params))
        styleRepository.delete(layerName, styleName)
        "Style ${styleName} for Layer ${layerName} deleted from ${type}"
    }

    @CliCommand(value = "style repository get", help = "Get a style from a style repository")
    String getStyleFromStyleRepository(
            @CliOption(key = "type", mandatory = true, help = "The type of style repository (directory, nested-directory, h2, sqlite, postgres)")
            String type,
            @CliOption(key = "options", mandatory = true, help = "The style repository options")
            String params,
            @CliOption(key = "layerName", mandatory = true, help = "The layer name")
            String layerName,
            @CliOption(key = "styleName", mandatory = true, help = "The style name")
            String styleName,
            @CliOption(key = "styleFile", mandatory = false, help = "The style file (sld or css)")
            File styleFile
    ) {
        StyleRepository styleRepository = StyleRepositoryFactory.getStyleRepository(type, StyleRepositoryFactory.getParameters(params))
        String style = styleRepository.getForLayer(layerName, styleName)
        if (styleFile) {
            styleFile.text = style
            "Style ${styleName} for Layer ${layerName} saved to ${styleFile.name}"
        } else {
            style
        }
    }

    @CliCommand(value = "style repository list", help = "List styles in a style repository")
    String listStylesInStyleRepository(
            @CliOption(key = "type", mandatory = true, help = "The type of style repository (directory, nested-directory, h2, sqlite, postgres)")
            String type,
            @CliOption(key = "options", mandatory = true, help = "The style repository options")
            String params,
            @CliOption(key = "layerName", mandatory = false, help = "The layer name")
            String layerName
    ) {
        StyleRepository styleRepository = StyleRepositoryFactory.getStyleRepository(type, StyleRepositoryFactory.getParameters(params))
        List<Map<String,String>> styles = []
        if (layerName) {
            styles.addAll(styleRepository.getForLayer(layerName))
        } else {
            styles.addAll(styleRepository.getAll())
        }
        String NEW_LINE = System.getProperty("line.separator")
        StringBuilder str = new StringBuilder()
        styles.each { Map<String,String> style ->
            str.append(style.layerName + " " + style.styleName).append(NEW_LINE)
        }
        str.toString()
    }

    @CliCommand(value = "style repository copy", help = "Copy styles from one repository to another")
    String copyStylesInStyleRepository(
            @CliOption(key = "inputType", mandatory = true, help = "The type of style repository (directory, nested-directory, h2, sqlite, postgres)")
            String inputType,
            @CliOption(key = "inputOptions", mandatory = true, help = "The style repository options")
            String inputParams,
            @CliOption(key = "outputType", mandatory = true, help = "The type of style repository (directory, nested-directory, h2, sqlite, postgres)")
            String outputType,
            @CliOption(key = "outputOptions", mandatory = true, help = "The style repository options")
            String outputParams
    ) {
        StyleRepository inputStyleRepository = StyleRepositoryFactory.getStyleRepository(inputType, StyleRepositoryFactory.getParameters(inputParams))
        StyleRepository outputStyleRepository = StyleRepositoryFactory.getStyleRepository(outputType, StyleRepositoryFactory.getParameters(outputParams))
        List<Map<String, String>> styles = inputStyleRepository.getAll()
        styles.each {Map<String,String> style ->
            outputStyleRepository.save(style.layerName, style.styleName, style.style)
        }
        "Copy styles from ${inputType} to ${outputType}"
    }

}
