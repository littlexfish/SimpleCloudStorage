package dev.littlexfish

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

@Serializable
data class LoginSession(val username: String)

fun Application.configureSession() {
	install(Sessions) {
		cookie<LoginSession>("LOGIN_SESSION") {
			cookie.extensions["SameSite"] = "lax"
		}
	}
}