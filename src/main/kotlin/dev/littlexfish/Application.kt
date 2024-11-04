package dev.littlexfish

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*

fun main() {
	prepareResource()
	embeddedServer(Netty, port = 9000, host = "0.0.0.0", module = Application::module)
		.start(wait = true)
}

val DEV_MODE = System.getProperty("io.ktor.development", "true") == "true"

fun prepareResource() {
}

fun Application.module() {
	install(CORS) {
		allowHeader("Content-Type")
		allowMethod(HttpMethod.Delete)
		allowMethod(HttpMethod.Put)
		if (DEV_MODE) {
			anyHost()
		}
	}
	install(ContentNegotiation) {
		removeIgnoredType<String>()
		jackson {
			registerKotlinModule()
		}
	}
//	configureSession()
	configureStatusPage()
	configureStaticPage()
	configureRouting()
}