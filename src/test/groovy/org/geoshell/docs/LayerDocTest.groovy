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
                    println "   ${cmd}"
                    File file = new File("src/main/docs/commands/${cmd.value()[0]}.txt")

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

}
