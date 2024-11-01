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
	}
}
