package linkCheck

import log
import java.net.URL

class LinkedResourcesRepository {

    /** Everything which is locally linked (HTML, images). */
    private val resources = mutableListOf<LinkedResource>()

    fun store(resource: LinkedResource) {
        if (resources.contains(resource)) {
            return
        }
        if (resource.url.toString().endsWith("/ch1-background/")) {
            throw Exception("fail") // FIXME remove me
        }
        log { "Storing: $resource" }
        resources += resource
    }

    // explicit downcast to provide read-only access
    fun allLinkedPages(): List<LinkedResource> = resources

    fun contains(url: URL): Boolean = resources.any { it.url == url }
}

data class LinkedResource(
    val url: URL
)
