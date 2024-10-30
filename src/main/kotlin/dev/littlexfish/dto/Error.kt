package dev.littlexfish.dto

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

data class Error(val code: HttpStatusCode, val message: String, val detail: String? = null)

suspend fun ApplicationCall.respondError(error: Error) {
	respond(error.code, error)
}
