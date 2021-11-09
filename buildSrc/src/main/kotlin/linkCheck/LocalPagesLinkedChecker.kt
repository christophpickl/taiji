package linkCheck

import log
import java.io.File
import java.net.URL

/**
 * Scan through all local build html files, see whether they have been linked at least once.
 */
class LocalPagesLinkedChecker(
    private val repo: LinkedResourcesRepository,
    private val localBuildDir: File,
    private val websiteBase: URL,
) {
    private val includedFileExtensions = if (Constants.EnableCheck.images) {
        Constants.includedNonHtmlFileExtensions + "html"
    } else {
        setOf("html")
    }

    fun check(): List<CheckError> {
        val localFiles: MutableMap<String, File> = collectAllLocalFilesInBuildDir()
            .associateBy { it.relativePath }.toMutableMap()
        val customErrors = mutableListOf<CheckError>()
        allLinkedPagesByCleanedRelativePath().distinct().forEach { pageRelativeUrl ->
            if (!localFiles.containsKey(pageRelativeUrl)) {
                customErrors += CheckError("Linked file not found locally: $pageRelativeUrl")
            }
            localFiles.remove(pageRelativeUrl)
        }

        return (localFiles.map {
            CheckError("Not linked local file: ${it.key}")
        } + customErrors).distinct().sorted()
    }

    private fun allLinkedPagesByCleanedRelativePath(): List<String> {
        val pages = repo.allLinkedPages()
        val baseLength = websiteBase.toString().length
        fun String.removeAnchor() = if (contains("#")) substringBefore("#") else this
        return pages.map { it.url.toString().substring(baseLength).removeAnchor() }
    }

    private fun collectAllLocalFilesInBuildDir(): List<File> {
        val files = mutableListOf<File>()
        scanFolder(files, localBuildDir)
        return files
    }

    private fun scanFolder(files: MutableList<File>, folder: File) {
        folder.listFiles()!!.forEach {
            if (it.isDirectory) {
                scanFolder(files, it)
            } else if (it.isFile && includedFileExtensions.contains(it.extension)) {
                files += it
            } else {
                log { "Going to ignore file: ${it.absolutePath}" }
            }
        }
    }

    private val File.relativePath: String get() = absolutePath.substring(localBuildDir.absolutePath.length)
}
