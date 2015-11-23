package org.geoshell

import geoscript.layer.Format
import geoscript.layer.Layer
import geoscript.layer.Raster
import geoscript.layer.TileLayer
import geoscript.workspace.Workspace
import org.geoshell.map.Map as GeoShellMap
import org.geoshell.map.MapName
import org.geoshell.raster.FormatName
import org.geoshell.raster.RasterName
import org.geoshell.tile.TileName
import org.geoshell.vector.LayerName
import org.geoshell.vector.WorkspaceName
import org.springframework.beans.factory.DisposableBean

class Catalog implements DisposableBean {

    Map<WorkspaceName, Workspace> workspaces = [:]

    Map<LayerName, Layer> layers = [:]

    Map<FormatName, Format> formats = [:]

    Map<RasterName, Raster> rasters = [:]

    Map<TileName, TileLayer> tiles = [:]

    Map<MapName, GeoShellMap> maps = [:]

    @Override
    void destroy() throws Exception {
        workspaces.each {WorkspaceName name, Workspace workspace ->
            workspace.close()
        }
        rasters.each {RasterName name, Raster raster ->
            raster.dispose()
        }
        tiles.each {TileName name, TileLayer tileLayer ->
            tileLayer.close()
        }
        workspaces.clear()
        layers.clear()
        formats.clear()
        rasters.clear()
        tiles.clear()
        maps.clear()
    }
}


