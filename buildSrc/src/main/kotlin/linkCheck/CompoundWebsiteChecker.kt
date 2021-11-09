package linkCheck

import java.io.File
import java.net.URL

class CompoundWebsiteChecker(
    private val localBuildDir: File,
    private val websiteBase: URL,
    private val websiteHomePagePath: String,
    private val linkCheckIgnore: Set<String>,
) {

    fun check(): List<CheckError> {
        val repo = LinkedResourcesRepository()
        LinkTargetValidChecker(repo, websiteBase, websiteHomePagePath, linkCheckIgnore).check().also { errors ->
            if (errors.isNotEmpty()) {
                return errors
            }
        }
        return LocalPagesLinkedChecker(repo, localBuildDir, websiteBase).check()
    }
}

data class CheckError(
    val message: String
) : Comparable<CheckError> {
    override fun compareTo(other: CheckError) = this.message.compareTo(other.message)
}
