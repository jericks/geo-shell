package org.geoshell

import geoscript.GeoScript
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.stereotype.Component

import java.awt.Desktop

@Component
class Commands implements CommandMarker {

    @CliCommand(value = "open", help = "Open a File.")
    void open(
            @CliOption(key = "file", mandatory = true, help = "The File") File file
    ) throws Exception {
        Desktop.desktop.open(file)
    }

    @CliCommand(value = "download", help = "Download a URL to a file.")
    void download(
            @CliOption(key = "url", mandatory = true, help = "The url") String url,
            @CliOption(key = "file", mandatory = true, help = "The file") File file,
            @CliOption(key = "overwrite", mandatory = false,
                    specifiedDefaultValue = "true", unspecifiedDefaultValue = "true",
                    help = "Whether to overwrite the file or not") boolean overwrite
    ) throws Exception {
        GeoScript.download(new URL(url), file, overwrite: overwrite)
    }

    @CliCommand(value = "unzip", help = "Unzip a file")
    void unzip(
            @CliOption(key = "file", mandatory = true, help = "The zip file") File file,
            @CliOption(key = "directory", mandatory = true, help = "The directory") File directory
    ) throws Exception {
        GeoScript.unzip(file, directory)
    }

}
