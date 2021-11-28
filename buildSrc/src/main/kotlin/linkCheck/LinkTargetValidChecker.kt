package linkCheck

import SampleConfig
import log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

fun main() {
    val repo = LinkedResourcesRepository()
    val errors = LinkTargetValidChecker(
        repo = repo,
        websiteBase = SampleConfig.websiteBase,
        websiteHomePagePath = SampleConfig.websiteHomePagePath,
        linkCheckIgnore = emptySet(),
    ).check()
    if (errors.isEmpty()) {
        println("OK")
    } else {
        println("Errors: $errors")
    }
}

/**
 * Scan through all links online whether they are ok (internal + external).
 */
class LinkTargetValidChecker(
    private val repo: LinkedResourcesRepository,
    private val websiteBase: URL,
    private val websiteHomePagePath: String,
    private val linkCheckIgnore: Set<String>
) {

    fun check(): List<CheckError> {
        val errors = mutableListOf<CheckError>()
        checkPage(errors, websiteHomePagePath, websiteBase)
        return errors
    }

    private fun checkPage(errors: MutableList<CheckError>, path: String, currentPage: URL) {
        val url = URI("$websiteBase$path").normalize().toURL()
        if (repo.contains(url)) {
            log { "Page already visited: $url" }
            return
        }

        val response = request(url)
        repo.store(LinkedResource(url))
        log { "Check page: $response" }

        val responseUrl = response.url.toString()
        val baseUrl = responseUrl.substring(0, responseUrl.lastIndexOf("/"))
        val currentBase = baseUrl.substring(websiteBase.toString().length)

        if (response.isOk) {
            val doc = Jsoup.parse(response.body)
            checkLinks(errors, url, doc, currentBase)
            checkImages(errors, doc, currentBase, currentPage)
        } else {
            errors += CheckError("Page fail for $currentPage: ${response.url} (${response.statusCode})")
        }
    }

    private fun checkLinks(errors: MutableList<CheckError>, url: URL, doc: Document, currentBase: String) {
        doc.select("a").forEach { a ->
            val href = a.attr("href")
            log { "Found href: [$href]" }
            when {
                href.isEmpty() -> {
                    // skip
                }
                href.startsWith("#") -> {
                    // anchor, skip
                }
                href.isHttpLink -> {
                    checkExternalLink(errors, href)
                }
                else -> {
                    // TODO this here is wrong, if i like to "/bar.html" it will go to the current base ("http://localhost/foo/bar.html"), but it should go to the "http://localhost/bar.html"
                    val cleanedHrefPath = if (href.startsWith("/")) href else "$currentBase/$href"
                    checkPage(errors, cleanedHrefPath, url)
                }
            }
        }
    }

    private fun checkExternalLink(errors: MutableList<CheckError>, href: String) {
        if (!Constants.EnableCheck.externalLinks) {
            return
        }
        if (!linkCheckIgnore.contains(href) && !request(URL(href)).isOk) {
            errors += CheckError("External link not found: $href")
        }
    }

    private fun checkImages(errors: MutableList<CheckError>, doc: Document, currentBase: String, currentPage: URL) {
        if (!Constants.EnableCheck.images) {
            return
        }
        doc.select("img").forEach { img ->
            val src = img.attr("src")
            if (src.isHttpLink) {
                if (!request(URL(src)).isOk) {
                    errors += CheckError("External image not found: $src")
                }
            } else {
                val cleanedSrcPath = if (src.startsWith("/")) src else "$currentBase/$src"
                val fullImageUrl = URI("$websiteBase$cleanedSrcPath").normalize().toURL()
                repo.store(LinkedResource(fullImageUrl))
                if (!request(fullImageUrl).isOk) {
                    errors += CheckError("Internal image not found on $currentPage: $fullImageUrl")
                }
            }
        }
    }

    private fun request(url: URL): Response {
        log { "Request: $url" }
        return try {
            val connection = url.openConnection() as HttpURLConnection
            val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                reader.readText()
            }
            Response(url, connection.responseCode, responseBody)
        } catch (e: FileNotFoundException) {
            Response(url, 404, e.message ?: "")
        } catch (e: IOException) {
            log { e.message ?: "" }
            Response(url, 999, e.message ?: "")
        }
    }

    private val String.isHttpLink: Boolean
        get() =
            startsWith("http://") || startsWith("https://")
}

private data class Response(
    val url: URL,
    val statusCode: Int,
    val body: String,
) {
    val isOk = statusCode == 200
    override fun toString() = "Response[$url => $statusCode]"
}
