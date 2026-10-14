package com.elma.botpro

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GeminiClient {
    /** Throws a readable IOException on HTTP/network errors so the UI can show the real cause. */
    suspend fun ask(apiKey: String, model: String, message: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw IOException("Clé API Gemini absente. Configure-la dans Paramètres > Clé Gemini.")
        var connection: HttpURLConnection? = null
        try {
            val safeModel = model.trim().ifBlank { "gemini-2.0-flash" }
            val key = URLEncoder.encode(apiKey.trim(), "UTF-8")
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$safeModel:generateContent?key=$key"
            connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15000
                readTimeout = 30000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            val prompt = "Tu es ELMA, un assistant naturel. Comprends les petites fautes, accents manquants, ponctuation et emojis. Réponds directement, clairement et naturellement en français. Question : $message"
            val body = JSONObject().put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            )).toString()
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val detail = runCatching { JSONObject(raw).optJSONObject("error")?.optString("message") }.getOrNull()
                throw IOException("Gemini HTTP $code${if (!detail.isNullOrBlank()) ": $detail" else ""}")
            }
            val json = JSONObject(raw)
            val candidates = json.optJSONArray("candidates")
            val parts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
            val answer = buildString {
                if (parts != null) for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)?.optString("text").orEmpty()
                    if (part.isNotBlank()) { if (isNotEmpty()) append("\n"); append(part) }
                }
            }.trim()
            if (answer.isBlank()) {
                val reason = candidates?.optJSONObject(0)?.optString("finishReason").orEmpty()
                throw IOException("Gemini n'a renvoyé aucun texte${if (reason.isNotBlank()) " (motif : $reason)" else ""}.")
            }
            answer
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Impossible de joindre Gemini : ${e.message ?: "vérifie Internet et la configuration"}", e)
        } finally {
            connection?.disconnect()
        }
    }
}
