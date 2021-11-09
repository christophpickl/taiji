// https://asciidoctor.github.io/asciidoctor-gradle-plugin/development-3.x/user-guide/
// doc: https://asciidoctor.github.io/asciidoctor-gradle-plugin/development-3.x/
// src: https://github.com/asciidoctor/asciidoctor-gradle-plugin
// project: https://asciidoctor.org/docs/asciidoctor-gradle-plugin/

// -Ptarget=local
val target: Target = Target.byId(project.findProperty("target") as? String ?: LocalTarget.id)

repositories {
//    jcenter() // don't delete, otherwise fails
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
        attributes = mapOf(
            // https://docs.asciidoctor.org/asciidoctor/latest/html-backend/default-stylesheet/#customize-docinfo
//            "docinfo" to "shared"
            "stylesheet" to "custom.css"
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
    LocalTarget -> tasks.register<Copy>("deploy") {
        doFirst {
            println("Local deploy to: ${LocalTarget.localWebRoot}")
        }
        from(Locations.htmlBuildOutput)
        //include("**/*.jpg")
        into(LocalTarget.localWebRoot)
    }
    RemoteTarget -> tasks.create<FtpDeployTask>("deploy") {
        ftpUrl = "f34-preview.awardspace.net"
        username = "3886058_cpsycho"
        (System.getenv("PSYWIKI_FTP_PASSWORD") as? String)?.let {
            password = it!!
        }
        // TODO register host: https://cp1.awardspace.net/start/
        remoteUploadDirectory = "/taijiwiki.scienceontheweb.net"
        localBuildDirectory = Locations.htmlBuildOutput.absolutePath
    }
}


//TODO tasks.named("deploy") {
//    dependsOn(tasks.clean, tasks.build)
//    dependsOn("clean", "build")
//}

// ./gradlew -q linkChecker
tasks.create<LinkCheckerTask>("linkChecker") {
    localBuildDirAbsPath = Locations.htmlBuildOutput.absolutePath
    websiteHomePagePath = "/index.html"
    linkCheckIgnore = setOf()
    checkTarget = target.id
}
