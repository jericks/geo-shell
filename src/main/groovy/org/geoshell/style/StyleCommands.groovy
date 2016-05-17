package org.geoshell.style

import geoscript.filter.Color
import geoscript.layer.Layer
import geoscript.style.Style
import geoscript.style.Symbolizer
import geoscript.style.io.SLDWriter
import geoscript.style.io.SimpleStyleReader
import geoscript.style.io.Writer
import geoscript.style.io.YSLDWriter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.geoshell.Catalog
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

    @CliCommand(value = "style default vector", help = "Create a simple style.")
    String createDefaultVector(
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

}
