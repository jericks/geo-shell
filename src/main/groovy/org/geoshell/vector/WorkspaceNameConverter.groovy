package org.geoshell.vector

import org.geoshell.Catalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component

@Component
class WorkspaceNameConverter implements Converter<WorkspaceName>{

    @Autowired
    Catalog catalog

    @Override
    boolean supports(Class<?> type, String s) {
        WorkspaceName.class.isAssignableFrom(type)
    }

    @Override
    WorkspaceName convertFromText(String value, Class<?> targetType, String optionContext) {
        new WorkspaceName(value)
    }

    @Override
    boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        completions.addAll(catalog.workspaces.keySet().collect{ new Completion(it.name) })
        true
    }
}
