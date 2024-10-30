package dev.littlexfish.dto

import dev.littlexfish.service.FileService
import java.io.File

data class DirectoryFiles(val path: String, val files: List<FileStruct>)

data class FileStruct(
	val name: String,
	val isHidden: Boolean,
	val isDirectory: Boolean,
	val size: Long,
//	val totalSize: Long,
	val lastModified: Long,
)

fun File.listToDirectoryFiles(): DirectoryFiles {
	return DirectoryFiles(
		path = FileService.getPath(this),
		files = listFiles()?.map(File::toFileStruct)?.sort() ?: emptyList()
	)
}

fun File.toFileStruct(): FileStruct {
	return FileStruct(
		name = name,
		isHidden = isHidden,
		isDirectory = isDirectory,
		size = length(),
//		totalSize = usableSpace,
		lastModified = lastModified()
	)
}

fun List<FileStruct>.sort(): List<FileStruct> {
	return sortedWith(compareBy({ !it.isDirectory }, { it.name }))
}