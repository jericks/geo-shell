package org.geoshell.tile

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@TupleConstructor
@EqualsAndHashCode
class TileName {
    String name
    @Override
    String toString() {
        name
    }
}