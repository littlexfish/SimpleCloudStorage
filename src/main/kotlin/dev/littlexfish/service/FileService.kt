package dev.littlexfish.service

import dev.littlexfish.dto.DirectoryStruct
import dev.littlexfish.dto.FileExistsState
import dev.littlexfish.dto.FilePreview
import dev.littlexfish.dto.Node
import java.io.File
import java.nio.CharBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object FileService {

	private val pathSepRegex = Regex("[/\\\\]+")

	private val scsHome = System.getProperty("SCS_HOME")?.let { File(it) } ?: File(System.getenv("user.dir"), ProgramService.NAME)
	private val rootFile = File(System.getProperty("SCS_ROOT") ?: System.getenv("SCS_ROOT") ?: ".").apply {
		if(isFile) {
			throw IllegalArgumentException("Root path must be a directory")
		}
		if(!exists()) {
			mkdirs()
		}
	}
	private val extensionTypeMap = mapOf(
		"jpg" to ViewExtensionType.IMAGE,
		"jpeg" to ViewExtensionType.IMAGE,
		"png" to ViewExtensionType.IMAGE,
		"gif" to ViewExtensionType.IMAGE,
		"bmp" to ViewExtensionType.IMAGE,
		"webp" to ViewExtensionType.IMAGE,

		"txt" to ViewExtensionType.TEXT,
		"log" to ViewExtensionType.TEXT,

		"pdf" to ViewExtensionType.PDF,

		"zip" to ViewExtensionType.ZIP,
		"jar" to ViewExtensionType.ZIP,
	)
	// Should be disabled, but admin can enable it
	private var traversalEnabled = false

	fun listFiles(parent: File): List<File> {
		if(!parent.exists()) {
			throw FileStateErrorException(FileStateErrorException.FileStateError.NOT_EXIST, parent)
		}
		if(parent.isFile) {
			throw FileStateErrorException(FileStateErrorException.FileStateError.NOT_DIRECTORY, parent)
		}
		if(!parent.canRead()) {
			throw FileStateErrorException(FileStateErrorException.FileStateError.NOT_READABLE, parent)
		}
		val relative = fileToRelative(parent)
		if(!traversalEnabled && relative.split(pathSepRegex).contains("..")) {
			throw FileStateErrorException(FileStateErrorException.FileStateError.TRAVERSAL_DISABLED, parent)
		}
		val list = parent.listFiles()?.toList() ?: emptyList()
		return list.filter {
			isPathValid(it.path)
		}
	}

	private fun fileToRelative(file: File): String {
		return file.toRelativeString(rootFile).replace('\\', '/') // force use / as separator
	}

	private fun isPathValid(path: String): Boolean {
		val resolve = rootFile.resolve(path)
		val block = ConfigService.config.blacklistPath.contains(resolve.path)
		return !(if (ConfigService.config.listReverse) !block else block)
	}

	fun getFile(path: String): File {
		val resolve = rootFile.resolve(path.removePrefix("/"))
		if(!resolve.exists()) {
			throw FileStateErrorException(FileStateErrorException.FileStateError.NOT_EXIST, resolve)
		}
		if(!resolve.canRead()) {
			throw FileStateErrorException(FileStateErrorException.FileStateError.NOT_READABLE, resolve)
		}
		return resolve
	}

	fun getPath(file: File): String {
		return fileToRelative(file).replace('\\', '/')
	}

	fun getFilePreview(file: File): FilePreview {
		val buffer = CharBuffer.allocate(ConfigService.config.maxPreviewFileSize)
		file.bufferedReader().use {
			it.read(buffer)
		}
		val hasTruncate = !buffer.hasRemaining()
		buffer.flip()
		return FilePreview(buffer.toString(), file.length(), hasTruncate)
	}

	fun getFileAsZip(file: File): DirectoryStruct {
		fun splitPath(path: String): List<String> {
			return path.split("/")
		}
		fun putEntry(entry: ZipEntry, nodes: ArrayList<Node>) {
			val spl = splitPath(entry.name)
			var currentNodes = nodes
			for (i in spl.dropLast(1).indices) {
				val name = spl[i]
				val node = currentNodes.find { it.name == name }
				if (node == null) {
					val l = ArrayList<Node>()
					val newNode = Node.Branch(name, l)
					currentNodes.add(newNode)
					currentNodes = l
				}
				else {
					currentNodes = (node as Node.Branch).children as ArrayList<Node>
				}
			}
			if (!entry.isDirectory) {
				currentNodes.add(Node.Leaf(spl.last()))
			}
		}
		val zip = ZipFile(file)
		val nodes = ArrayList<Node>()
		zip.entries().asIterator().forEach { putEntry(it, nodes) }
		zip.close()
		return DirectoryStruct(file.path, nodes)
	}

	fun getExtensionType(extension: String): ViewExtensionType {
		return extensionTypeMap[extension] ?: ViewExtensionType.UNKNOWN
	}

	fun getSCSFile(name: String): File {
		return File(scsHome, name)
	}

	fun getExistsStatus(path: String, fileNames: List<String>): List<FileExistsState> {
		val parent = getFile(path)
		return fileNames.map {
			val file = File(parent, it)
			val isDirectory = if (file.exists()) file.isDirectory else null
			FileExistsState(it, isDirectory)
		}
	}

	fun createFile(path: String, name: String): File {
		return File(getFile(path), name)
	}

	class FileStateErrorException(state: FileStateError, file: File) : RuntimeException("${state.message}: ${fileToRelative(file)}") {

		enum class FileStateError(val message: String) {
			NOT_EXIST("File does not exist"),
			NOT_FILE("Path is not a file"),
			NOT_DIRECTORY("Path is not a directory"),
			NOT_READABLE("Path is not readable"),
			NOT_WRITABLE("Path is not writable"),
			TRAVERSAL_DISABLED("Traversal is disabled"),
		}

	}

}