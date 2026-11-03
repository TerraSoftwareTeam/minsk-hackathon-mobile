package helpers

import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.DependencyHandlerScope

fun DependencyHandlerScope.addCatalogBundle(
    configurationName: String,
    catalog: VersionCatalog,
    bundleName: String
) {
    catalog.findBundle(bundleName).get().get().forEach { dependency ->
        add(configurationName, dependency)
    }
}
