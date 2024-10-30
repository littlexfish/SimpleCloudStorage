package dev.littlexfish.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.Reader

object TranslateService {

	private val builtinLanguageCode = listOf(
		"en-US",

	)

	private val dynLangDir = System.getProperty("SCS_LANG")?.let { File(it) } ?: FileService.getSCSFile("i18n")
	private val langCodeRegex = "[a-zA-Z]{2}-[a-zA-Z]{2}".toRegex()
	private val map = mutableMapOf<String, TranslateLanguage>()
	private const val DEFAULT_CODE = "en-US"
	private val defaultLanguage: TranslateLanguage get() = map[DEFAULT_CODE]!!

	fun autoLoadTranslateFile() {
		// Load builtin language
		builtinLanguageCode.forEach { code ->
			javaClass.getResourceAsStream("/i18n/$code.json")!!.reader().use { loadTranslatable(code, it) }
		}
		// Load other languages
		if (!dynLangDir.isDirectory) return
		dynLangDir.list()?.forEach { name ->
			val code = name.removeSuffix(".json")
			if (langCodeRegex.matches(code)) {
				val file = File(dynLangDir, name)
				if (!file.isFile) return@forEach
				file.reader().use { loadTranslatable(code, it) }
			}
		}
	}

	fun loadTranslatable(code: String, from: Reader) {
		val lang = TranslateLanguage(code.lowercase())
		val node = jacksonObjectMapper().readTree(from)
		flatMap(lang, node)
		map[lang.code] = lang
	}

	fun getLoadedCodes() = map.keys.toList()

	private fun flatMap(lang: TranslateLanguage, node: JsonNode, prefix: String = "") {
		if (node.isObject) {
			node.fields().forEach { (key, value) ->
				flatMap(lang, value, if (prefix.isEmpty()) key else "$prefix.$key")
			}
		}
		else if (node.isArray) {
			node.forEachIndexed { index, value ->
				flatMap(lang, value, "$prefix[$index]")
			}
		}
		else if (node.isTextual) {
			lang[prefix.lowercase()] = node.textValue()
		}
	}

	private fun getTranslateLanguage(code: String? = null): TranslateLanguage {
		if (code == null) return defaultLanguage
		val codeLower = code.lowercase()
		var find = map[codeLower]
		if (find != null) return find
		val split = codeLower.split('-')
		if (split.size == 2) {
			find = map[split[0]]
			if (find != null) return find
		}
		return defaultLanguage
	}

	fun translate(key: String, code: String? = null): String {
		return getTranslateLanguage(code)[key]
	}

}

class TranslateLanguage(val lang: String, val region: String? = null) {

	val code = "$lang${region?.let { "-$it" } ?: ""}"

	private val map = mutableMapOf<String, String>()

	operator fun get(key: String): String {
		return map[key] ?: key
	}

	internal operator fun set(key: String, value: String) {
		map[key] = value
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as TranslateLanguage
		return code == other.code
	}

	override fun hashCode(): Int {
		return code.hashCode()
	}

}

fun tr(id: String, code: String? = null): String {
	return TranslateService.translate(id, code)
}
