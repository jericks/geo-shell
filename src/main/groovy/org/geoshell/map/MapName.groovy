package org.geoshell.map

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@TupleConstructor
@EqualsAndHashCode
class MapName {
    String name
    @Override
    String toString() {
        name
    }
}
