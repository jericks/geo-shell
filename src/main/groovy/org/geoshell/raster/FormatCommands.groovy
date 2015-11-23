package org.geoshell.raster

import geoscript.layer.Format
import org.geoshell.Catalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.shell.support.util.OsUtils
import org.springframework.stereotype.Component

@Component
class FormatCommands implements CommandMarker {

    @Autowired
    Catalog catalog

    @CliCommand(value = "format open", help = "Open a Raster Format.")
    String open(
            @CliOption(key = "name", mandatory = false, help = "The Format name") FormatName name,
            @CliOption(key = "input", mandatory = true, help = "The input string") String input
    ) throws Exception {
        Format format = Format.getFormat(new File(input))
        if (format) {
            catalog.formats[name] = format
            "Format ${name} opened!"
        } else {
            "Unable to open connection to Format using ${input}"
        }
    }

    @CliCommand(value = "format close", help = "Close a Raster Format.")
    String close(
            @CliOption(key = "name", mandatory = true, help = "The Format name") FormatName name
    ) throws Exception {
        Format format = catalog.formats[name]
        if (format) {
            catalog.formats.remove(name)
            "Format ${name} closed!"
        } else {
            "Unable to find Format ${name}"
        }
    }

    @CliCommand(value = "format list", help = "List open Raster Formats.")
    String list() throws Exception {
        catalog.formats.collect{FormatName name, Format f ->
            "${name} = ${f.name}"
        }.join(OsUtils.LINE_SEPARATOR)
    }

    @CliCommand(value = "format rasters", help = "List the Rasters in a Format.")
    String rasters(
            @CliOption(key = "name", mandatory = true, help = "The Format name") FormatName name
    ) throws Exception {
        Format format = catalog.formats[name]
        format.names.collect{String rasterName ->
            rasterName
        }.join(OsUtils.LINE_SEPARATOR)
    }


}
