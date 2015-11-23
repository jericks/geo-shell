package org.geoshell.map

import geoscript.geom.Bounds
import geoscript.layer.Layer
import geoscript.proj.Projection
import org.geoshell.Catalog
import org.geoshell.raster.RasterName
import org.geoshell.tile.TileName
import org.geoshell.vector.LayerName

class Map {

    MapName name

    private Catalog catalog

    private List<MapLayerName> mapLayerNames = []

    private java.util.Map<MapLayerName, Object> layerMap = [:]

    Map(MapName name, Catalog catalog) {
        this.name = name
        this.catalog = catalog
    }

    List<MapLayerName> getMapLayerNames() {
        mapLayerNames
    }

    Object getLayer(MapLayerName layerName) {
        layerMap[layerName]
    }

    void addLayer(MapLayerName name, Object layer) {
        mapLayerNames.add(name)
        layerMap[name] = layer
    }

    void removeLayer(MapLayerName name) {
        mapLayerNames.remove(name)
        layerMap.remove(name)
    }

    String moveMapLayer(MapLayerName layerName, String order) {
        int numberOfLayers = mapLayerNames.size()
        int oldIndex = mapLayerNames.indexOf(layerName)
        if (oldIndex == -1) {
            "Unable to find ${layerName} in ${name}"
        } else {
            int newIndex
            // relative +1, -2
            if (order.startsWith("+") || order.startsWith("-")) {
                String operator = order.substring(0,1)
                int index = order.substring(1).toInteger()
                if (operator.equals("+")) {
                    newIndex = oldIndex - index
                } else {
                    newIndex = oldIndex + index
                }
            }
            // absolute
            else if (order.isInteger()) {
                newIndex = order.toInteger()
                if (newIndex < 0) {
                    newIndex = 0;
                } else if (newIndex > numberOfLayers) {
                    newIndex = numberOfLayers
                }
            }
            // top, bottom, up, down
            else if (order.equalsIgnoreCase("top")) {
                newIndex = 0
            } else if (order.equalsIgnoreCase("bottom")) {
                newIndex = mapLayerNames.size()
            } else if (order.equalsIgnoreCase("up")) {
                newIndex = Math.max(0, oldIndex - 1)
            } else if (order.equalsIgnoreCase("down")) {
                newIndex = Math.max(numberOfLayers, oldIndex + 1)
            }
            mapLayerNames.remove(oldIndex)
            mapLayerNames.add(newIndex, layerName)
            "Moved ${layerName} from ${oldIndex} to ${newIndex}"
        }
    }

    geoscript.render.Map getMap(java.util.Map options = [:]) {
        String type = options.get("type","png")
        int width = options.get("width", 400)
        int height = options.get("height", 300)
        String backgroundColor = options.get("backgroundColor")
        Projection projection = options.get("projection")
        Bounds bounds = options.get("bounds")
        geoscript.render.Map map = new geoscript.render.Map(
                layers: this.getLayers(),
                type: type,
                width: width,
                height: height,
                backgroundColor: backgroundColor
        )
        if (projection) {
            map.proj = projection
        }
        if (bounds) {
            map.bounds = bounds
        }
        map
    }

    List getLayers() {
        List layerList = []
        this.mapLayerNames.collect { MapLayerName name ->
            Object layerName = layerMap[name]
            if (layerName instanceof LayerName && catalog.layers.containsKey(layerName)) {
                layerList.add(catalog.layers.get(layerName as LayerName) as Layer)
            } else if (layerName instanceof RasterName && catalog.rasters.containsKey(layerName)) {
                layerList.add(catalog.rasters.get(layerName as RasterName))
            } else if (layerName instanceof TileName && catalog.tiles.containsKey(layerName)) {
                layerList.add(catalog.tiles.get(layerName))
            }/*else {
                // Not found, it was probably closed, so remove it!
                removeLayer(name)
            }*/
        }
        layerList
    }

}
