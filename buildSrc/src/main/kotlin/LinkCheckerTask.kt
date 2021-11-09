import linkCheck.CheckError
import linkCheck.CompoundWebsiteChecker
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildException
import java.io.File
import java.net.URL

fun main() {
    LinkCheckerTask().apply {
        localBuildDirAbsPath = SampleConfig.localBuildDirAbsPath
        checkTarget = LocalTarget.id
        websiteHomePagePath = SampleConfig.websiteHomePagePath
        runCommand()
    }
}

object SampleConfig {
    val localBuildDirAbsPath = "/Users/wu/workspace/psycho.wiki/build/output/html"
    val websiteBaseUrl = "http://psywiki.scienceontheweb.net"
    val websiteBase = URL(websiteBaseUrl)
    val websiteHomePagePath = "/index.html"
}


open class LinkCheckerTask : DefaultTask() {

    @Input
    lateinit var localBuildDirAbsPath: String

    @Input
    lateinit var websiteHomePagePath: String

    @Input
    lateinit var linkCheckIgnore: Set<String>

    @Input
    lateinit var checkTarget: String

    @TaskAction
    fun runCommand() {
        val errors = validateLinks()
        if (errors.isEmpty()) {
            reportSuccess()
        } else {
            reportError(errors)
        }
    }

    private fun validateLinks(): List<CheckError> {
        val target = Target.byId(checkTarget)
        println("Checking links for: target=$target localBuildDir=[$localBuildDirAbsPath]")
        return CompoundWebsiteChecker(
            File(localBuildDirAbsPath),
            URL(target.websiteBaseUrl),
            websiteHomePagePath,
            linkCheckIgnore
        ).check()
    }

    private fun reportSuccess() {
        println("\u001B[32mAll good\u001B[0m ✅")
    }

    private fun reportError(errors: List<CheckError>) {
        throw BuildException(
            "Check errors occured! See logs for details.\n" +
                    "\n\u001B[31mErrors occurred\u001B[0m ❌\n\n" +
                    errors.joinToString("\n") { "- ${it.message}" }, null
        )
    }
}
