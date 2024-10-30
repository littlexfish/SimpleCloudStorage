package dev.littlexfish

import dev.littlexfish.dto.respondError
import dev.littlexfish.dto.Error
import dev.littlexfish.routing.*
import dev.littlexfish.service.FileService
import dev.littlexfish.style.styleBase
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
//			call.respondHtml {
//				head {
//					title { +"Error" }
//				}
//				body {
//					h1 { +"500: Internal Server Error" }
//					if(DEV_MODE) {
//						pre { +cause.stackTraceToString() }
//					}
//					else {
//						p { +"$cause" }
//					}
//				}
//			}
//			call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
			call.respondError(Error(HttpStatusCode.InternalServerError, cause.message ?: "Internal Server Error"))
		}
	}
}

fun Application.configureStaticPage() {
	routing {
		// Static plugin. Try to access `/static/index.html`
//		staticResources("/static", "static")
		staticFiles("/static", webStatic)
	}
}

fun Application.configureRouting() {
	routing {
		route("/api") {
			apiDirectoryView()
			apiFileView()
		}
//		route("/page") {
//			routeDirectoryView()
//		}
	}
}

