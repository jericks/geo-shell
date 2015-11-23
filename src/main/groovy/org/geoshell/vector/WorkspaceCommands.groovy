package org.geoshell.vector

import geoscript.workspace.Memory
import geoscript.workspace.Workspace
import org.geoshell.Catalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.CommandMarker
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.shell.support.util.OsUtils
import org.springframework.stereotype.Component

@Component
class WorkspaceCommands implements CommandMarker {

    @Autowired
    Catalog catalog

    @CliCommand(value = "workspace open", help = "Open a Workspace.")
    String open(
            @CliOption(key = "name", mandatory = true, help = "The Workspace name") WorkspaceName name,
            @CliOption(key = "params", mandatory = true, help = "The connection parameters") String params
    ) throws Exception {
        Workspace workspace = params.equalsIgnoreCase("memory") ? new Memory() : Workspace.getWorkspace(params);
        if (workspace) {
            catalog.workspaces[name] = workspace
            "Workspace ${name} opened!"
        } else {
            "Unable to open connection to Workspace using ${params}"
        }
    }

    @CliCommand(value = "workspace close", help = "Close a Workspace.")
    String close(
            @CliOption(key = "name", mandatory = true, help = "The Workspace name") WorkspaceName name
    ) throws Exception {
        Workspace workspace = catalog.workspaces[name]
        if (workspace) {
            workspace.close()
            catalog.workspaces.remove(name)
            "Workspace ${name} closed!"
        } else {
            "Unable to find Workspace ${name}"
        }
    }

    @CliCommand(value = "workspace list", help = "List open Workspaces.")
    String list() throws Exception {
        catalog.workspaces.collect{WorkspaceName name, Workspace w ->
            "${name} = ${w.format}"
        }.join(OsUtils.LINE_SEPARATOR)
    }

    @CliCommand(value = "workspace layers", help = "List the Layer in a Workspaces.")
    String layers(
            @CliOption(key = "name", mandatory = true, help = "The Workspace name") WorkspaceName name
    ) throws Exception {
        Workspace workspace = catalog.workspaces[name]
        workspace.names.collect{String layerName ->
            layerName
        }.join(OsUtils.LINE_SEPARATOR)
    }

}
