package dev.littlexfish.service

import dev.littlexfish.dto.DirectoryStruct
import dev.littlexfish.dto.Node
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object FileService {

	private val scsHome = System.getProperty("SCS_HOME")?.let { File(it) } ?: File(System.getenv("user.dir"), ProgramService.NAME)
	private val rootFile = File(System.getProperty("SCS_ROOT") ?: System.getenv("SCS_ROOT") ?: ".").apply {
		if(isFile) {
			throw IllegalArgumentException("Root path must be a directory")
		}
		if(!exists()) {
			mkdirs()
		}
	}
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
		if(!traversalEnabled && relative.split(File.pathSeparator).contains("..")) {
			throw IllegalStateException("Traversal is disabled")
		}
		val list = parent.listFiles()?.toList() ?: emptyList()
		return list.filter {
			isPathValid(it.path)
		}
	}

	private fun fileToRelative(file: File): String {
		return file.toRelativeString(rootFile)
	}

	private fun isPathValid(path: String): Boolean {
		val resolve = rootFile.resolve(path)
		val block = ConfigService.config.blacklistPath.contains(resolve.path)
		return !(if (ConfigService.config.listReverse) !block else block)
	}

	fun getFile(path: String): File {
		val resolve = rootFile.resolve(path)
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

	fun getSCSFile(name: String): File {
		return File(scsHome, name)
	}

	class FileStateErrorException(state: FileStateError, file: File) : RuntimeException("${state.message}: $file") {

		enum class FileStateError(val message: String) {
			NOT_EXIST("File does not exist"),
			NOT_FILE("Path is not a file"),
			NOT_DIRECTORY("Path is not a directory"),
			NOT_READABLE("Path is not readable"),
			NOT_WRITABLE("Path is not writable"),
		}

	}

}