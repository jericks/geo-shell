package org.geoshell.tile

import org.geoshell.Catalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component

@Component
class TileNameConverter implements Converter<TileName> {

    @Autowired
    Catalog catalog

    @Override
    boolean supports(Class<?> type, String s) {
        TileName.class.isAssignableFrom(type)
    }

    @Override
    TileName convertFromText(String value, Class<?> targetType, String optionContext) {
        new TileName(value)
    }

    @Override
    boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        completions.addAll(catalog.tiles.keySet().collect { new Completion(it.name) })
        true
    }
}
