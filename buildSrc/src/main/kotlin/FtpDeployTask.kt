import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTP
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress

open class FtpDeployTask : DefaultTask() {

    @Input
    lateinit var ftpUrl: String

    @Input
    lateinit var username: String

    @Input
    lateinit var password: String

    @Input
    lateinit var localBuildDirectory: String

    private val localBuildDirectoryFile by lazy { File(localBuildDirectory) }

    @Input
    lateinit var remoteUploadDirectory: String

    @Internal
    override fun getGroup() = "publish"

    private val fileTypeAscii = listOf("html")
    private val fileTypeBinary = listOf("jpg", "png")

    @TaskAction
    fun runCommand() {
        require(localBuildDirectoryFile.exists()) { "Build directory doesnt exist at: ${localBuildDirectoryFile.absolutePath}" }
        val ftp = FTPClient()
        println("Connecting to: $ftpUrl")
        ftp.connect(InetAddress.getByName(ftpUrl))
        println("Reply connect: ${ftp.replyString}")
        try {
            require(ftp.login(username, password)) { "Login failed: ${ftp.replyString}" }
            try {
                ftp.uploadWiki()
            } finally {
                ftp.logout()
            }
        } finally {
            ftp.disconnect()
        }
    }

    private fun FTPClient.uploadWiki() {
        require(isConnected)
        require(isAvailable)
        enterLocalPassiveMode()
        uploadDirectory(localBuildDirectoryFile)
    }

    private fun FTPClient.uploadDirectory(directory: File) {
        println("Current local directory: ${directory.absolutePath}")
        val remoteDir =
            "$remoteUploadDirectory${directory.absolutePath.substring(localBuildDirectoryFile.absolutePath.length)}"
        println("Changing to remote Directory: $remoteDir")
        makeDirectory(remoteDir)
        require(changeWorkingDirectory(remoteDir)) { "Failed to change working directory to: $remoteDir" }
        val (files, dirs) = directory.listFiles()!!.partition { it.isFile }
        files.forEach { uploadFile(it) }
        dirs.forEach { uploadDirectory(it) }
    }

    private fun FTPClient.uploadFile(file: File) {
        println("Upload: ${file.absolutePath}")
        setFileType(file.ftpFileType)
        BufferedInputStream(FileInputStream(file)).use { fileBuffer ->
            require(storeFile(file.name, fileBuffer)) { "Failed to store file: ${file.absolutePath}" }
        }
    }

    private val File.ftpFileType: Int
        get() = when {
            fileTypeAscii.contains(extension) -> FTP.ASCII_FILE_TYPE
            fileTypeBinary.contains(extension) -> FTP.BINARY_FILE_TYPE
            else -> throw Exception("Can't determine FTP file type of: $absolutePath")
        }
}
