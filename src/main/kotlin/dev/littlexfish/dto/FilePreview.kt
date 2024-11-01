package dev.littlexfish.dto

data class FilePreview(
	val content: String,
	val size: Long,
	val truncate: Boolean
)
