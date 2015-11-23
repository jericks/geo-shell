package org.geoshell.raster

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@TupleConstructor
@EqualsAndHashCode
class FormatName {
    String name
    @Override
    String toString() {
        name
    }
}
