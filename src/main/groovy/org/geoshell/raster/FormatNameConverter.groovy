package org.geoshell.raster

import org.geoshell.Catalog
import org.geoshell.tile.TileName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component

@Component
class FormatNameConverter implements Converter<FormatName> {

    @Autowired
    Catalog catalog

    @Override
    boolean supports(Class<?> type, String s) {
        FormatName.class.isAssignableFrom(type)
    }

    @Override
    FormatName convertFromText(String value, Class<?> targetType, String optionContext) {
        new FormatName(value)
    }

    @Override
    boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        completions.addAll(catalog.formats.keySet().collect { new Completion(it.name) })
        true
    }
}
