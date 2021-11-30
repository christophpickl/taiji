object Constants {
    //    const val LOG_ENABLED = false
    const val LOG_ENABLED = true

    object EnableCheck {
        const val images = true // website hoster makes it difficult to fetch images alone ;)
        const val externalLinks = false
    }

    val includedNonHtmlFileExtensions = setOf("jpg", "png", "pdf")

    val localWebsiteBasePath = "http://localhost/~cpickl/taijiwiki"
}
