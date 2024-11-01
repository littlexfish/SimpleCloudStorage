package dev.littlexfish.routing

import dev.littlexfish.dto.Error
import dev.littlexfish.dto.respondError
import dev.littlexfish.service.ConfigService
import dev.littlexfish.service.FileService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
					"viewable" to ConfigService.config.allowedViewExtensions.contains(file.extension)
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
	}
}
