package org.geoshell.map

import org.geoshell.Catalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component

@Component
class MapNameConverter implements Converter<MapName> {

    @Autowired
    Catalog catalog

    @Override
    boolean supports(Class<?> type, String s) {
        MapName.class.isAssignableFrom(type)
    }

    @Override
    MapName convertFromText(String value, Class<?> targetType, String optionContext) {
        new MapName(value)
    }

    @Override
    boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        completions.addAll(catalog.maps.keySet().collect { new Completion(it.name) })
        true
    }
}
