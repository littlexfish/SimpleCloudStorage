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
	var allowedViewExtensions: Map<String, ViewExtensionType> = mapOf(
		"jpg" to ViewExtensionType.IMAGE,
		"jpeg" to ViewExtensionType.IMAGE,
		"png" to ViewExtensionType.IMAGE,
		"gif" to ViewExtensionType.IMAGE,
		"bmp" to ViewExtensionType.IMAGE,
		"webp" to ViewExtensionType.IMAGE,

		"txt" to ViewExtensionType.TEXT,
		"log" to ViewExtensionType.TEXT,

		"pdf" to ViewExtensionType.PDF,

		"zip" to ViewExtensionType.ZIP,
	)

}


enum class ViewExtensionType {
	IMAGE, TEXT, PDF, ZIP
}
