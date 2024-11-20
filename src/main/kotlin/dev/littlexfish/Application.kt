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
	val server: String = System.getenv("SCS_SERVER") ?: "0.0.0.0"
	val port: Int = System.getenv("SCS_PORT")?.toIntOrNull() ?: 8080
	embeddedServer(Netty, port = port, host = server, module = Application::module)
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