import org.gradle.api.Project

val Project.Locations get() = ProjectExtensions(this)

class ProjectExtensions(private val project: Project) {
    val sourceDirectory = project.file("asciidoc")
    val htmlBuildOutput = project.file("${project.buildDir}/output/html")
}
