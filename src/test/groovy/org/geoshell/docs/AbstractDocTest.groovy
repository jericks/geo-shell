package org.geoshell.docs

import org.junit.After
import org.junit.Before
import org.springframework.shell.Bootstrap
import org.springframework.shell.core.CommandResult
import org.springframework.shell.core.JLineShellComponent

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.StreamHandler

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
        boolean captureOutput = options.get("captureOutput", false)
        String output
        if (captureOutput) {
            ByteArrayOutputStream outContent = new ByteArrayOutputStream()
            Logger log = Logger.getLogger("")
            Handler[] handlers = log.getHandlers();
            StreamHandler customLogHandler = new StreamHandler(outContent, handlers[0].getFormatter())
            log.addHandler(customLogHandler)
            log.setLevel(Level.INFO)
            CommandResult result = shell.executeCommand(cmd)
            customLogHandler.flush()
            output = outContent.toString()
            log.removeHandler(customLogHandler)
        } else {
            CommandResult result = shell.executeCommand(cmd)
            output = result.result.toString()
        }
        writeFile("${name}_command", processCommand(cmd, options))
        writeFile("${name}_result", processOutput(output, options))
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
            output.split("\n").findAll {
                !it.contains("org.springframework.shell")
            }.collect { String line ->
                line = scrubOutput(line)
                line.isEmpty() ? "" : "[green]#${line}# +"
            }.join("\n")
        }
    }

    String scrubOutput(String str) {
        str = str.trim()
        if (str.contains("INFO: ")) {
            str.substring(str.indexOf("INFO: ") + 6)
        } else {
            str
        }
    }

}
