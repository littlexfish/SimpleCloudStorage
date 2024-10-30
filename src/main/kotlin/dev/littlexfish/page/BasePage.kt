package dev.littlexfish.page

import dev.littlexfish.service.ProgramService
import io.ktor.server.html.*
import kotlinx.html.*

class BasePage : Template<HTML> {

	var pageTitle: String = ProgramService.SIMPLE_NAME
	var subTitle: String? = null
	var content: Placeholder<FlowContent> = Placeholder()

	override fun HTML.apply() {
		head {
			title { +(pageTitle + (if (subTitle != null) " - $subTitle" else "")) }
			link(rel = "stylesheet", href = "/style/base.css")
		}
		body {
			insert(content)
		}
	}

}