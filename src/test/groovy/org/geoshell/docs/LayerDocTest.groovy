package org.geoshell.docs

import org.junit.Test
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.stereotype.Component

import java.lang.reflect.Method
import java.lang.reflect.Parameter

class LayerDocTest extends AbstractDocTest {

    @Test
    void document() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true)
        provider.addIncludeFilter(new AnnotationTypeFilter(Component))
        provider.findCandidateComponents("org.geoshell").each { BeanDefinition beanDefinition ->
            println beanDefinition.getBeanClassName()
            Class clazz = Class.forName(beanDefinition.beanClassName)
            clazz.declaredMethods.each { Method method ->
                method.getAnnotationsByType(CliCommand).each { CliCommand cmd ->


                    String commandName = cmd.value()[0]
                    String commandHelp = cmd.help()
                    List<Map> parameters = []

                    method.parameters.each { Parameter parameter ->
                        parameter.getAnnotationsByType(CliOption).each { CliOption cliOption ->
                            parameters.add([
                                key: cliOption.key()[0],
                                help: cliOption.help(),
                                mandatory: cliOption.mandatory(),
                                specifiedDefaultValue: cliOption.specifiedDefaultValue() != "__NULL__" ? cliOption.specifiedDefaultValue() : "",
                                unspecifiedDefaultValue: cliOption.unspecifiedDefaultValue() != "__NULL__" ? cliOption.unspecifiedDefaultValue() : "",

                            ])
                        }
                    }

                    String text = ""
                    if (!parameters.isEmpty()) {
                        text += "\n"
                        text += "|===\n"
                        text += "|Name "
                        text += "|Description "
                        text += "|Mandatory "
                        text += "|Specified Default "
                        text += "|Unspecified Default "
                        text += "\n"
                        parameters.each { Map parameter ->
                            text += "|${parameter.key}\n"
                            text += "|${parameter.help}\n"
                            text += "|${parameter.mandatory}\n"
                            text += "|${parameter.specifiedDefaultValue}\n"
                            text += "|${parameter.unspecifiedDefaultValue}\n"
                            text += "\n"
                        }
                        text += "|===\n"
                    }

                    File file = new File("src/main/docs/commands/${commandName.replaceAll(' ', '_')}.txt")
                    if (!file.parentFile.exists()) {
                        file.parentFile.mkdir()
                    }
                    file.text = text
                }
            }
        }
    }

    @Test
    void createRandomPoints() {
        run("layer_random", [
                "workspace open --name layers --params memory",
                "layer random --output-workspace layers --output-name points --geometry -180,-90,180,90 --number 100 --projection EPSG:4326",
                "style vector default --layer points --color #1E90FF --file examples/points.sld",
                "layer style set --name points --style examples/points.sld",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name randomMap",
                "map add layer --name randomMap --layer ocean",
                "map add layer --name randomMap --layer countries",
                "map add layer --name randomMap --layer points",
                "map draw --name randomMap --file examples/random_points.png",
                "map close --name randomMap"
        ])
        copyFile(new File("examples/random_points.png"), new File("src/main/docs/images"))
    }

    @Test
    void buffer() {
        run("layer_buffer", [
                "workspace open --name layers --params memory",
                "layer random --output-workspace layers --output-name points --geometry -180,-90,180,90 --number 100 --projection EPSG:4326",
                "layer buffer --input-name points --output-workspace layers --output-name buffers --distance 10",
                "style vector default --layer points --color #1E90FF --file examples/points.sld",
                "style vector default --layer buffers --color #1E90FF --opacity 0.25 --file examples/buffers.sld",
                "layer style set --name points --style examples/points.sld",
                "layer style set --name buffers --style examples/buffers.sld",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name map",
                "map add layer --name map --layer ocean",
                "map add layer --name map --layer countries",
                "map add layer --name map --layer buffers",
                "map add layer --name map --layer points",
                "map draw --name map --file examples/layer_buffer.png",
                "map close --name map"
        ])
        copyFile(new File("examples/layer_buffer.png"), new File("src/main/docs/images"))
    }

    @Test
    void centroid() {
        run("layer_centroid", [
                "workspace open --name layers --params memory",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer centroid --input-name countries --output-name centroids --output-workspace layers",
                "style vector default --layer centroids --color #1E90FF --file examples/centroids.sld",
                "layer style set --name centroids --style examples/centroids.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name map",
                "map add layer --name map --layer ocean",
                "map add layer --name map --layer countries",
                "map add layer --name map --layer centroids",
                "map draw --name map --file examples/layer_centroid.png",
                "map close --name map"
        ])
        copyFile(new File("examples/layer_centroid.png"), new File("src/main/docs/images"))
    }

    @Test
    void interiorpoint() {
        run("layer_interiorpoint", [
                "workspace open --name layers --params memory",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer interiorpoint --input-name countries --output-name interiorpoints --output-workspace layers",
                "style vector default --layer interiorpoints --color #1E90FF --file examples/interiorpoints.sld",
                "layer style set --name interiorpoints --style examples/interiorpoints.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name map",
                "map add layer --name map --layer ocean",
                "map add layer --name map --layer countries",
                "map add layer --name map --layer interiorpoints",
                "map draw --name map --file examples/layer_interiorpoint.png",
                "map close --name map"
        ])
        copyFile(new File("examples/layer_interiorpoint.png"), new File("src/main/docs/images"))
    }

    @Test
    void extent() {
        run("layer_extent", [
                "workspace open --name layers --params memory",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer style set --name states --style examples/states.sld",
                "layer open --workspace naturalearth --layer states --name states",
                "layer extent --input-name states --output-workspace layers --output-name usa",
                "style vector default --layer usa --color #1E90FF --opacity 0.25 --file examples/extent.sld",
                "layer style set --name usa --style examples/extent.sld",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name map",
                "map add layer --name map --layer ocean",
                "map add layer --name map --layer countries",
                "map add layer --name map --layer states",
                "map add layer --name map --layer usa",
                "map draw --name map --file examples/layer_extent.png",
                "map close --name map"
        ])
        copyFile(new File("examples/layer_extent.png"), new File("src/main/docs/images"))
    }

    @Test
    void extents() {
        run("layer_extents", [
                "workspace open --name layers --params memory",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer style set --name states --style examples/states.sld",
                "layer open --workspace naturalearth --layer states --name states",
                "layer extents --input-name states --output-workspace layers --output-name state_extents",
                "style vector default --layer state_extents --color #1E90FF --opacity 0.25 --file examples/extent.sld",
                "layer style set --name state_extents --style examples/extent.sld",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name map",
                "map add layer --name map --layer ocean",
                "map add layer --name map --layer countries",
                "map add layer --name map --layer states",
                "map add layer --name map --layer state_extents",
                "map draw --name map --file examples/layer_extents.png",
                "map close --name map"
        ])
        copyFile(new File("examples/layer_extents.png"), new File("src/main/docs/images"))
    }


    @Test
    void createSquareGraticules() {
        run("layer_graticule_square", [
            "workspace open --name layers --params memory",
            "layer graticule square --workspace layers --name squares --bounds -180,-90,180,90 --length 20",
            "style vector default --layer squares --color #1E90FF --opacity 0.30 --file examples/squares.sld",
            "layer style set --name squares --style examples/squares.sld",
            "workspace open --name naturalearth --params examples/naturalearth.gpkg",
            "layer open --workspace naturalearth --layer countries --name countries",
            "layer style set --name countries --style examples/countries.sld",
            "layer open --workspace naturalearth --layer ocean --name ocean",
            "layer style set --name ocean --style examples/ocean.sld",
            "map open --name graticule",
            "map add layer --name graticule --layer ocean",
            "map add layer --name graticule --layer countries",
            "map add layer --name graticule --layer squares",
            "map draw --name graticule --file examples/square_graticules.png",
            "map close --name graticule"
        ])
        copyFile(new File("examples/square_graticules.png"), new File("src/main/docs/images"))
    }

    @Test
    void createRectangleGraticules() {
        run("layer_graticule_rectangle", [
                "workspace open --name layers --params memory",
                "layer graticule rectangle --workspace layers --name rectangles --bounds -180,-90,180,90 --width 20 --height 10",
                "style vector default --layer rectangles --color #1E90FF --opacity 0.30 --file examples/rectangles.sld",
                "layer style set --name rectangles --style examples/rectangles.sld",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name graticule",
                "map add layer --name graticule --layer ocean",
                "map add layer --name graticule --layer countries",
                "map add layer --name graticule --layer rectangles",
                "map draw --name graticule --file examples/rectangle_graticules.png",
                "map close --name graticule"
        ])
        copyFile(new File("examples/rectangle_graticules.png"), new File("src/main/docs/images"))
    }

    @Test
    void createOvalGraticules() {
        run("layer_graticule_oval", [
                "workspace open --name layers --params memory",
                "layer graticule oval --workspace layers --name ovals --bounds -180,-90,180,90 --size 20",
                "style vector default --layer ovals --color #1E90FF --opacity 0.30 --file examples/ovals.sld",
                "layer style set --name ovals --style examples/ovals.sld",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name graticule",
                "map add layer --name graticule --layer ocean",
                "map add layer --name graticule --layer countries",
                "map add layer --name graticule --layer ovals",
                "map draw --name graticule --file examples/oval_graticules.png",
                "map close --name graticule"
        ])
        copyFile(new File("examples/oval_graticules.png"), new File("src/main/docs/images"))
    }

    @Test
    void createHexagonGraticules() {
        run("layer_graticule_hexagon", [
                "workspace open --name layers --params memory",
                "layer graticule hexagon --workspace layers --name hexagons --bounds -180,-90,180,90 --length 10",
                "style vector default --layer hexagons --color #1E90FF --opacity 0.30 --file examples/hexagons.sld",
                "layer style set --name hexagons --style examples/hexagons.sld",
                "workspace open --name naturalearth --params examples/naturalearth.gpkg",
                "layer open --workspace naturalearth --layer countries --name countries",
                "layer style set --name countries --style examples/countries.sld",
                "layer open --workspace naturalearth --layer ocean --name ocean",
                "layer style set --name ocean --style examples/ocean.sld",
                "map open --name graticule",
                "map add layer --name graticule --layer ocean",
                "map add layer --name graticule --layer countries",
                "map add layer --name graticule --layer hexagons",
                "map draw --name graticule --file examples/hexagon_graticules.png",
                "map close --name graticule"
        ])
        copyFile(new File("examples/hexagon_graticules.png"), new File("src/main/docs/images"))
    }

}
