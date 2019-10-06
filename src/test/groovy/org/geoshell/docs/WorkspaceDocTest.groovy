package org.geoshell.docs

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.shell.Bootstrap
import org.springframework.shell.core.CommandResult
import org.springframework.shell.core.JLineShellComponent

class WorkspaceDocTest {

    private JLineShellComponent shell;

    @Before
    void before() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
        shell = bootstrap.getJLineShellComponent()
    }

    @After
    void after() {
        shell.stop()
    }


    @Test
    void workspaceOpenListClose() {
        run([
                "workspace_basics_open":  "workspace open --name mem --params memory",
                "workspace_basics_list":  "workspace list",
                "workspace_basics_close": "workspace close --name mem"
        ])
    }

    @Test
    void workspaceLayers() {
        run([
                "workspace_layers_open":   "workspace open --name naturalearth --params src/test/resources/naturalearth.gpkg",
                "workspace_layers_layers": "workspace layers --name naturalearth",
                "workspace_layers_close":  "workspace close --name naturalearth"
        ])
    }

    void run(Map<String,String> commands) {
        commands.each { String name, String command ->
            run(name, command)
        }
    }

    String run(String name, String cmd) {
        CommandResult result = shell.executeCommand(cmd)
        writeFile("${name}_command", processCommand(cmd))
        writeFile("${name}_result", processOutput(result.result.toString()))
    }

    void writeFile(String name, String text) {
        File dir = new File("src/main/docs/output")
        if (!dir.exists()) {
            dir.mkdir()
        }
        File file = new File(dir, "${name}.txt")
        file.text = text
    }

    String processCommand(String cmd) {

        String name
        String params

        int firstParam = cmd.indexOf("--")
        if (firstParam > -1) {
            name = cmd.substring(0,firstParam).trim()
            params = cmd.substring(firstParam).trim()
        } else {
            name = cmd.trim()
            params = ""
        }
        String styledName =  "[navy]*${name}*"
        String styleParams = params.trim() == "" ? "" : params.split(" ").collect { String param ->
           param = param.trim()
           if (param.startsWith("--")) {
               "[gray]#${param}#"
           } else {
               "[silver]#${param}#"
           }
        }.join(' ')

        "[blue]#geo-shell># ${styledName} ${styleParams} +"
    }

    String processOutput(String output) {
        output.split("\n").collect { "[green]#${it}# +" }.join("\n")
    }

}
