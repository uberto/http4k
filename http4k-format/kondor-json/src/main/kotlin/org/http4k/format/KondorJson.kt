package org.http4k.format

typealias MappingCustomConfigurator =AutoMappingConfiguration<JConverterResolver>.() -> AutoMappingConfiguration<JConverterResolver>
object KondorJson : ConfigurableKondorJson({
    asConfigurable()
        .withStandardMappings()
        .done()
}) {
    fun custom(configure:MappingCustomConfigurator) =
        CustomKondorJson(configure)
}

class CustomKondorJson(configure:MappingCustomConfigurator): ConfigurableKondorJson({asConfigurable().withStandardMappings().let(configure).done()})


