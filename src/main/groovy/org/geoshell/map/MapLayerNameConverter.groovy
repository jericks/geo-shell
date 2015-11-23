package org.geoshell.map

import org.geoshell.Catalog
import org.geoshell.map.Map as GeoShellMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component

@Component
class MapLayerNameConverter implements Converter<MapLayerName> {

    @Autowired
    Catalog catalog

    @Override
    boolean supports(Class<?> type, String s) {
        MapLayerName.class.isAssignableFrom(type)
    }

    @Override
    MapLayerName convertFromText(String value, Class<?> targetType, String optionContext) {
        new MapLayerName(value)
    }

    @Override
    boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        String line = target.remainingBuffer
        String nameParam = "--name"
        if (line.contains(nameParam)) {
            int start = line.indexOf(nameParam) + nameParam.length()
            int end = line.indexOf("--", start)
            MapName mapName = new MapName(line.substring(start, end).trim())
            completions.addAll(catalog.maps[mapName].layers.collect { new Completion(it.name) })
        } else {
            catalog.maps.each {MapName mapName, GeoShellMap map ->
                completions.addAll(map.layers.collect { new Completion(it.name) })
            }
        }
        true
    }
}
