package org.geoshell;

import org.geotools.data.ogr.OGRDataStoreFactory;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.logging.Level;

@Component
public class Lifecycle {

    @Autowired
    Catalog catalog;

    @PostConstruct
    public void setup() {
        Logging.getLogger(OGRDataStoreFactory.class).setLevel(Level.OFF);
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
