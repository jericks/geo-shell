package org.geoshell.vector

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

@TupleConstructor
@EqualsAndHashCode
class WorkspaceName {
    String name
    @Override
    String toString() {
        name
    }
}
