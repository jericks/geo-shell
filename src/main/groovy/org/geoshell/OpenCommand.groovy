package org.geoshell

import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.stereotype.Component

import java.awt.Desktop

@Component
class OpenCommand implements CommandMarker {

    @CliCommand(value = "open", help = "Open a File.")
    void open(
            @CliOption(key = "file", mandatory = true, help = "The File") File file
    ) throws Exception {
        Desktop.desktop.open(file)
    }

}
