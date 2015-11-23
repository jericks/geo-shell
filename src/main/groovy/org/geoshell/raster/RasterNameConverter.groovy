package org.geoshell.raster

import org.geoshell.Catalog
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component

@Component
class RasterNameConverter implements Converter<RasterName> {

    @Autowired
    Catalog catalog

    @Override
    boolean supports(Class<?> type, String s) {
        RasterName.class.isAssignableFrom(type)
    }

    @Override
    RasterName convertFromText(String value, Class<?> targetType, String optionContext) {
        new RasterName(value)
    }

    @Override
    boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        String line = target.remainingBuffer
        String formatParam = "--format"
        if (line.contains(formatParam)) {
            int start = line.indexOf(formatParam) + formatParam.length()
            int end = line.indexOf("--", start)
            FormatName formatName = new FormatName(line.substring(start, end).trim())
            completions.addAll(catalog.formats[formatName].names.collect { new Completion(it) })
        } else {
            completions.addAll(catalog.rasters.keySet().collect { new Completion(it.name) })
        }
        true
    }
}
