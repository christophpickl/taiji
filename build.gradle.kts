repositories {
    mavenCentral()
}

plugins {
    id("org.asciidoctor.jvm.convert") version "3.1.0"
}

tasks {
    "asciidoctor"(org.asciidoctor.gradle.jvm.AsciidoctorTask::class) {
        doFirst {
            println("asciidoctor: source=${Locations.sourceDirectory} output=${Locations.htmlBuildOutput}")
        }
        // options(mapOf("doctype" to "book", "ruby" to "erubis"))
        attributes = mapOf(
            "stylesheet" to "custom.css",
            "toc" to "left",
        )
        sourceDir(Locations.sourceDirectory)
        setOutputDir(Locations.htmlBuildOutput)
    }
}

val copyResourcesTask = tasks.register<Copy>("copyResources") {
    doFirst {
        println("Copying resources from ${Locations.sourceDirectory} to ${Locations.htmlBuildOutput}")
    }
    from(Locations.sourceDirectory)
    include(*Constants.includedNonHtmlFileExtensions.map { "**/*.$it" }.toList().toTypedArray())
    into(Locations.htmlBuildOutput)
}

tasks.build {
    dependsOn(copyResourcesTask)
    dependsOn("asciidoctor")
}

tasks.register<GradleBuild>("deploy") {
    tasks = listOf("copyLocalDocs", "deployLocalSite")
}

tasks.register<Copy>("copyLocalDocs") {
    val targetDir = File(project.projectDir, "docs")
    doFirst {
        println("Local copy to: $targetDir")
    }
    from(Locations.htmlBuildOutput)
    into(targetDir)
}

tasks.register<Copy>("deployLocalSite") {
    doFirst {
        println("Local deploy to: ${LocalTarget.localWebRoot}")
    }
    from(Locations.htmlBuildOutput)
    into(LocalTarget.localWebRoot)
}

tasks.create<LinkCheckerTask>("linkChecker") {
    localBuildDirAbsPath = Locations.htmlBuildOutput.absolutePath
    websiteHomePagePath = "/index.html"
    linkCheckIgnore = setOf()
}
