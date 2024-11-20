package dev.littlexfish.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object ConfigService {

	private val configFile = FileService.getSCSFile("config.json").apply {
		if(!exists()) {
			outputStream().use { jacksonObjectMapper().writeValue(it, Config()) }
		}
	}

	private var internalConfig: Config? = null
	val config: Config get() {
		if(internalConfig == null) {
			loadConfig()
		}
		return internalConfig!!
	}

	fun loadConfig() {
		internalConfig = jacksonObjectMapper().readValue<Config>(configFile)
	}

	fun saveConfig() {
		if (internalConfig == null) {
			internalConfig = Config()
		}
		internalConfig.let { jacksonObjectMapper().writeValue(configFile, it) }
	}

}

class Config {

	/**
	 * Blacklist of paths
	 */
	var blacklistPath: List<String> = listOf()

	/**
	 * Reverse the list
	 */
	var listReverse: Boolean = false

	/**
	 * Allowed view extensions
	 */
	var allowedPreviewExtensions: Set<String> = setOf(
		"jpg", "jpeg", "png", "gif", "bmp", "webp",
		"txt", "log",
		"pdf",
		"zip"
	)

	/**
	 * Max file preview length, default 1MB
	 */
	var maxPreviewFileSize = 1024 * 1024

}


enum class ViewExtensionType {
	IMAGE, TEXT, PDF, ZIP, UNKNOWN
}
