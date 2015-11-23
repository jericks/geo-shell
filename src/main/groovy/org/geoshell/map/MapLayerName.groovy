package org.geoshell.map

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@TupleConstructor
@EqualsAndHashCode
class MapLayerName {
    String name
    @Override
    String toString() {
        name
    }
}
