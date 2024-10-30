package dev.littlexfish

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.littlexfish.service.TranslateService
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import kotlinx.css.CSSBuilder

fun main() {
	prepareResource()
	println(DEV_MODE)
	embeddedServer(Netty, port = 9000, host = "0.0.0.0", module = Application::module)
		.start(wait = true)
}

val DEV_MODE = System.getProperty("io.ktor.development", "true") == "true"

fun prepareResource() {
	TranslateService.autoLoadTranslateFile()
}

fun Application.module() {
	install(CORS) {
		if (DEV_MODE) {
			anyHost()
		}
	}
	install(ContentNegotiation) {
		jackson {
			registerKotlinModule()
		}
	}
//	configureSession()
	configureStatusPage()
	configureStaticPage()
	configureRouting()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
	this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}