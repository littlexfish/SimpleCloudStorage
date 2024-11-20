package dev.littlexfish.routing

import dev.littlexfish.dto.Error
import dev.littlexfish.dto.respondError
import dev.littlexfish.service.ConfigService
import dev.littlexfish.service.FileService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*

private val pathNotFileError = Error(HttpStatusCode.BadRequest, "Path is not a file")

fun Route.apiFileView() {
	route("/file") {
		get("/download") {
			val path = call.request.queryParameters["path"] ?: "."
			val file = FileService.getFile(path)
			if (file.isFile) {
				call.respondFile(file)
			}
			else {
				call.respondError(pathNotFileError)
			}
		}
		get("/type") {
			val path = call.request.queryParameters["path"] ?: "."
			val file = FileService.getFile(path)
			if (file.isFile) {
				call.respond(mapOf(
					"type" to FileService.getExtensionType(file.extension),
					"viewable" to (ConfigService.config.allowedPreviewExtensions.contains(file.extension) &&
							file.length() <= ConfigService.config.maxPreviewFileSize)
				))
			}
			else {
				call.respondError(pathNotFileError)
			}
		}
		get("/text") {
			val path = call.request.queryParameters["path"] ?: "."
			val file = FileService.getFile(path)
			if (file.isFile) {
				call.respond(FileService.getFilePreview(file))
			}
			else {
				call.respondError(pathNotFileError)
			}
		}
		get("/zip") {
			val path = call.request.queryParameters["path"] ?: "."
			val file = FileService.getFile(path)
			if (file.isFile) {
				call.respond(FileService.getFileAsZip(file))
			}
			else {
				call.respondError(pathNotFileError)
			}
		}
		post("/exists") {
			val path = call.request.queryParameters["path"] ?: "."
			val fileNames = call.receive<List<String>>()
			call.respond(FileService.getExistsStatus(path, fileNames))
		}
		post("/upload") {
			val path = call.request.queryParameters["path"] ?: "."
			val successNames = mutableListOf<String>()
			call.receiveMultipart().forEachPart { part ->
				if (part is PartData.FileItem) {
					val name = part.originalFileName ?: "file"
					val file = FileService.createFile(path, name)
					file.writeChannel().use {
						part.provider().copyAndClose(this)
					}
					successNames.add(name)
				}
			}
			call.respond(successNames)
		}
		delete("/delete") {
			val path = call.request.queryParameters["path"] ?: "."
			val recursive = call.request.queryParameters["recursive"]?.toBoolean() ?: false
			val file = FileService.getFile(path)
			if (!recursive && file.list()?.isEmpty() == false) {
				return@delete call.respondError(Error(HttpStatusCode.BadRequest, "Directory is not empty"))
			}
			if (recursive) {
				file.deleteRecursively()
			}
			else {
				file.delete()
			}
			call.respond(mapOf("path" to FileService.getPath(file)))
		}
		put("/rename") {
			val path = call.request.queryParameters["path"] ?: "."
			val name = call.receive<String>()
			val parent = path.substringBeforeLast('/', ".")
			val file = FileService.getFile(path)
			val newFile = FileService.createFile(parent, name)
			file.renameTo(newFile)
			call.respond(mapOf("path" to FileService.getPath(newFile)))
		}
	}
}
