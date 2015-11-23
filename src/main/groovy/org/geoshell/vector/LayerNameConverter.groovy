package org.geoshell.vector

import org.geoshell.Catalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component

@Component
class LayerNameConverter implements Converter<LayerName> {

    @Autowired
    Catalog catalog

    @Override
    boolean supports(Class<?> type, String s) {
        LayerName.class.isAssignableFrom(type)
    }

    @Override
    LayerName convertFromText(String value, Class<?> targetType, String optionContext) {
        new LayerName(value)
    }

    @Override
    boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        String line = target.remainingBuffer
        String workspaceParam = "--workspace"
        if (line.contains(workspaceParam)) {
            int start = line.indexOf(workspaceParam) + workspaceParam.length()
            int end = line.indexOf("--", start)
            WorkspaceName workspaceName = new WorkspaceName(line.substring(start, end).trim())
            completions.addAll(catalog.workspaces[workspaceName].names.collect { new Completion(it) })
        } else {
            completions.addAll(catalog.layers.keySet().collect { new Completion(it.name) })
        }
        true
    }
}
