package org.geoshell.docs

import org.junit.After
import org.junit.Before
import org.springframework.shell.Bootstrap
import org.springframework.shell.core.CommandResult
import org.springframework.shell.core.JLineShellComponent

abstract class AbstractDocTest {

    protected JLineShellComponent shell

    @Before
    void before() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap()
        shell = bootstrap.getJLineShellComponent()
    }

    @After
    void after() {
        shell.stop()
    }

    void run(String name, List<String> commands, Map options = [:]) {
        commands.eachWithIndex { String command, int i ->
            run("${name}_${i}", command, options)
        }
    }

    void run(Map<String,String> commands, Map options = [:]) {
        commands.each { String name, String command ->
            run(name, command, options)
        }
    }

    String run(String name, String cmd, Map options = [:]) {
        CommandResult result = shell.executeCommand(cmd)
        writeFile("${name}_command", processCommand(cmd, options))
        writeFile("${name}_result", processOutput(result.result.toString(), options))
    }

    void writeFile(String name, String text) {
        File dir = new File("src/main/docs/output")
        if (!dir.exists()) {
            dir.mkdir()
        }
        File file = new File(dir, "${name}.txt")
        file.text = text
    }

    File copyFile(File fromFile, File toDir) {
        if (!toDir.exists()) {
            toDir.mkdir()
        }
        File toFile = new File(toDir, fromFile.name)
        fromFile.withInputStream { InputStream inputStream ->
            toFile.withOutputStream { OutputStream outputStream ->
                outputStream << inputStream
            }
        }
        toFile
    }

    String processCommand(String cmd, Map options = [:]) {

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

    String processOutput(String output, Map options = [:]) {
        if (options.rawOutput) {
            "----\n" + output + "\n----\n"
        } else {
            output.split("\n").collect { "[green]#${it.trim()}# +" }.join("\n")
        }
    }

}
