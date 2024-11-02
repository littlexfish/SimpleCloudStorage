package dev.littlexfish.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

data class Error(@get:JsonIgnore val code: HttpStatusCode, val message: String, val detail: String? = null) {

	@get:JsonProperty("code")
	val statusCode: Int
		get() = code.value

}

suspend fun ApplicationCall.respondError(error: Error) {
	respond(error.code, error)
}
