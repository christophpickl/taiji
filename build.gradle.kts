// -Ptarget=local
val target: Target = Target.byId(project.findProperty("target") as? String ?: LocalTarget.id)

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

when (target) {
    LocalTarget -> {
        tasks.register<GradleBuild>("deploy") {
            tasks = listOf("copyDocs", "deploySite")
        }
        tasks.register<Copy>("copyDocs") {
            val targetDir = File(project.projectDir, "docs")
            doFirst {
                println("Local copy to: $targetDir")
            }
            from(Locations.htmlBuildOutput)
            into(targetDir)
        }
        tasks.register<Copy>("deploySite") {
            doFirst {
                println("Local deploy to: ${LocalTarget.localWebRoot}")
            }
            from(Locations.htmlBuildOutput)
            into(LocalTarget.localWebRoot)
        }
    }
    RemoteTarget -> tasks.create<FtpDeployTask>("deploy") {
        ftpUrl = "f34-preview.awardspace.net"
        username = "3886058_cpsycho"
        (System.getenv("PSYWIKI_FTP_PASSWORD") as? String)?.let {
            password = it!!
        }
        remoteUploadDirectory = "/taijiwiki.scienceontheweb.net"
        localBuildDirectory = Locations.htmlBuildOutput.absolutePath
    }
}

// ./gradlew -q linkChecker
tasks.create<LinkCheckerTask>("linkChecker") {
    localBuildDirAbsPath = Locations.htmlBuildOutput.absolutePath
    websiteHomePagePath = "/index.html"
    linkCheckIgnore = setOf()
    checkTarget = target.id
}
