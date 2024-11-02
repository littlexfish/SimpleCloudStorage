package dev.littlexfish

import dev.littlexfish.dto.respondError
import dev.littlexfish.dto.Error
import dev.littlexfish.routing.*
import dev.littlexfish.service.FileService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import java.io.File

private val webStatic = (System.getProperty("SCS_WEB_STATIC")?.let { File(it) } ?: FileService.getSCSFile("static")).apply { mkdirs() }

fun Application.configureStatusPage() {
	install(StatusPages) {
		exception<Throwable> { call, cause ->
			when (cause) {
				is FileService.FileStateErrorException -> {
					call.respondError(Error(HttpStatusCode.BadRequest, cause.message ?: "Bad Request"))
				}
				else -> {
					cause.printStackTrace()
					call.respondError(Error(HttpStatusCode.InternalServerError, cause.message ?: "Internal Server Error"))
				}
			}
		}
	}
}

fun Application.configureStaticPage() {
	routing {
		staticFiles("/static", webStatic)
	}
}

fun Application.configureRouting() {
	routing {
		route("/api") {
			apiDirectoryView()
			apiFileView()
		}
	}
}

