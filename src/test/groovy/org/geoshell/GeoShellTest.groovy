package org.geoshell

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.shell.Bootstrap
import org.springframework.shell.core.CommandResult
import org.springframework.shell.core.JLineShellComponent

class GeoShellTest {

    private JLineShellComponent shell;

    @BeforeEach
    void before() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
        shell = bootstrap.getJLineShellComponent()
    }

    @AfterEach
    void after() {
        shell.stop()
    }

    @Test
    void workspace() {
        [
            [
                command: "workspace open --name mem --params memory",
                result:  "Workspace mem opened!"
            ],
            [
                command: "workspace list",
                result:  "mem = Memory"
            ],
            [
                command: "workspace close --name mem",
                result:  "Workspace mem closed!"
            ]
        ].each { Map cmd ->
            CommandResult result = shell.executeCommand(cmd.command)
            assertEquals cmd.result, result.result.toString()
        }
    }

    @Test
    void layerDelaunay() {
        [
            [
                    command: "workspace open --name mem --params memory",
                    result:  "Workspace mem opened!"
            ],
            [
                    command: "layer random --geometry \"0,0,45,45\" --projection EPSG:4326 " +
                             "--number 100 --output-workspace mem --output-name points",
                    result:  "Done!"
            ],
            [
                    command: "layer delaunay --input-name points --output-workspace mem --output-name delaunay",
                    result:  "Done!"
            ]
        ].each { Map cmd ->
            CommandResult result = shell.executeCommand(cmd.command)
            assertEquals cmd.result, result.result.toString()
        }
    }

}