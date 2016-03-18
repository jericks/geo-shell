package org.geoshell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class Lifecycle {

    @Autowired
    Catalog catalog;

    @PostConstruct
    public void setup() {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    @PreDestroy
    public void teardown() {
        try {
            catalog.destroy();
        } catch (Exception ex) {
            // We tried...
        }
    }

}
