package dev.littlexfish.routing

import dev.littlexfish.dto.Error
import dev.littlexfish.dto.listToDirectoryFiles
import dev.littlexfish.dto.respondError
import dev.littlexfish.service.FileService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val pathNotDirectoryError = Error(HttpStatusCode.BadRequest, "Path is not a directory")

fun Route.apiDirectoryView() {
	route("/directory") {
		get {
			val path = call.request.queryParameters["path"] ?: ""
			val file = FileService.getFile(path)
			if (file.isDirectory) {
				call.respond(file.listToDirectoryFiles())
			}
			else {
				call.respondError(pathNotDirectoryError)
			}
		}
		post("/create") {
			val path = call.request.queryParameters["path"] ?: ""
			val name = path.substringAfterLast('/', path)
			val parent = path.substringBeforeLast('/', ".")
			val file = FileService.createFile(parent, name)
			if (!file.exists()) {
				val suc = file.mkdirs()
				if (!suc) {
					call.respondError(Error(HttpStatusCode.BadRequest, "Failed to create directory"))
				}
				else {
					call.respond(mapOf("path" to FileService.getPath(file)))
				}
			}
			else {
				call.respondError(Error(HttpStatusCode.BadRequest, "Failed to create file"))
			}
		}
	}
}
