package com.elma.botpro

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

class BrainStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("elma_bot", Context.MODE_PRIVATE)
    private val brainKey = "brain_json"
    private val configKey = "config_json"

    fun loadBrain(): JSONObject {
        val saved = prefs.getString(brainKey, null)
        if (saved != null) return runCatching { JSONObject(saved) }.getOrElse { defaultBrain() }
        val asset = runCatching {
            context.assets.open("subconscient_elma.json").bufferedReader().use { it.readText() }
        }.getOrNull()
        return runCatching { JSONObject(asset ?: "") }.getOrElse { defaultBrain() }
    }

    fun saveBrain(brain: JSONObject) { prefs.edit().putString(brainKey, brain.toString()).apply() }

    fun loadConfig(): JSONObject {
        val assetConfig = runCatching {
            context.assets.open("vic_config.json").bufferedReader().use { JSONObject(it.readText()) }
        }.getOrElse { JSONObject() }
        val saved = prefs.getString(configKey, null)
        val config = if (saved != null) {
            val stored = runCatching { JSONObject(saved) }.getOrElse { JSONObject() }
            // Stored settings override defaults; an empty stored key does not hide a key in the asset.
            val merged = JSONObject(assetConfig.toString())
            val keys = stored.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key != "ai_key" || stored.optString(key).isNotBlank()) merged.put(key, stored.get(key))
            }
            merged
        } else assetConfig

        // Accept the legacy and common Gemini key names while keeping ai_key canonical.
        if (config.optString("ai_key").isBlank()) {
            val gemini = config.optJSONObject("gemini")
            val key = sequenceOf(
                config.optString("gemini_api_key"),
                config.optString("api_key"),
                gemini?.optString("api_key").orEmpty(),
                gemini?.optString("key").orEmpty()
            ).firstOrNull { it.isNotBlank() }
            if (key != null) config.put("ai_key", key)
        }
        if (config.optString("model").isBlank()) config.put("model", "gemini-2.0-flash")
        return config
    }

    fun saveConfig(config: JSONObject) { prefs.edit().putString(configKey, config.toString()).apply() }

    fun normalize(input: String): String {
        val noMarks = Normalizer.normalize(input.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return noMarks.replace('’', '\'').replace(Regex("[!?.,;:(){}\\[\\]\"/\\\\]+"), " ")
            .replace(Regex("\\s+"), " ").trim()
    }

    private fun similarity(a0: String, b0: String): Double {
        val a = normalize(a0); val b = normalize(b0)
        if (a.isEmpty() || b.isEmpty()) return 0.0
        if (a == b) return 1.0
        if (a.contains(b) || b.contains(a)) return max(0.88, jaroWinkler(a, b))
        return jaroWinkler(a, b)
    }

    // Similarité de Jaro-Winkler, légère et sans dépendance externe.
    private fun jaroWinkler(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0
        val range = (max(s1.length, s2.length) / 2 - 1).coerceAtLeast(0)
        val m1 = BooleanArray(s1.length); val m2 = BooleanArray(s2.length)
        var matches = 0
        for (i in s1.indices) {
            for (j in (i-range).coerceAtLeast(0)..(i+range).coerceAtMost(s2.lastIndex)) {
                if (!m2[j] && s1[i] == s2[j]) { m1[i] = true; m2[j] = true; matches++; break }
            }
        }
        if (matches == 0) return 0.0
        var k = 0; var transpositions = 0
        for (i in s1.indices) if (m1[i]) {
            while (k < s2.length && !m2[k]) k++
            if (k < s2.length && s1[i] != s2[k]) transpositions++
            k++
        }
        val m = matches.toDouble()
        val jaro = (m/s1.length + m/s2.length + (m-transpositions/2.0)/m) / 3.0
        var prefix = 0
        while (prefix < minOf(4, s1.length, s2.length) && s1[prefix] == s2[prefix]) prefix++
        return jaro + prefix * 0.1 * (1.0-jaro)
    }

    fun findLocalAnswer(message: String): String? {
        val brain = loadBrain()
        val threshold = brain.optJSONObject("parametres")?.optDouble("similarite_minimum", 0.78) ?: 0.78
        var bestScore = 0.0
        var responses = emptyList<String>()
        val rules = brain.optJSONArray("regles") ?: JSONArray()
        for (i in 0 until rules.length()) {
            val rule = rules.optJSONObject(i) ?: continue
            if (!rule.optBoolean("active", true)) continue
            val triggers = rule.optJSONArray("declencheurs") ?: JSONArray()
            val replies = rule.optJSONArray("reponses") ?: JSONArray()
            for (j in 0 until triggers.length()) {
                val score = similarity(message, triggers.optString(j))
                if (score > bestScore) {
                    bestScore = score
                    responses = (0 until replies.length()).map { replies.optString(it) }.filter { it.isNotBlank() }
                }
            }
        }
        val learned = brain.optJSONArray("connaissances_apprises") ?: JSONArray()
        for (i in 0 until learned.length()) {
            val item = learned.optJSONObject(i) ?: continue
            val q = item.optString("question_normalisee", item.optString("question"))
            val score = similarity(message, q)
            if (score > bestScore) {
                bestScore = score
                val arr = item.optJSONArray("reponses") ?: JSONArray()
                responses = (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
            }
        }
        return if (bestScore >= threshold && responses.isNotEmpty()) responses[Random.nextInt(responses.size)] else null
    }

    fun saveLearnedAnswer(question: String, answer: String) {
        val brain = loadBrain()
        val learned = brain.optJSONArray("connaissances_apprises") ?: JSONArray().also {
            brain.put("connaissances_apprises", it)
        }
        val normalized = normalize(question)
        var target: JSONObject? = null
        for (i in 0 until learned.length()) {
            val item = learned.optJSONObject(i) ?: continue
            if (similarity(normalized, item.optString("question_normalisee", "")) >= 0.88) {
                target = item
                break
            }
        }
        if (target == null) {
            val replies = JSONArray().put(answer)
            learned.put(JSONObject().put("question", question)
                .put("question_normalisee", normalized).put("reponses", replies))
        } else {
            val replies = target.optJSONArray("reponses") ?: JSONArray().also { target.put("reponses", it) }
            var exists = false
            for (i in 0 until replies.length()) if (replies.optString(i) == answer) exists = true
            if (!exists) replies.put(answer)
        }
        saveBrain(brain)
    }

    fun brainAsText(): String {
        val brain = loadBrain()
        val out = StringBuilder("# CERVEAU ELMA — format : mot-clé | mot-clé => réponse || réponse\n# Les commentaires commencent par #.\n\n")
        val rules = brain.optJSONArray("regles") ?: JSONArray()
        for (i in 0 until rules.length()) {
            val rule = rules.optJSONObject(i) ?: continue
            val t = rule.optJSONArray("declencheurs") ?: JSONArray()
            val r = rule.optJSONArray("reponses") ?: JSONArray()
            out.append((0 until t.length()).map { t.optString(it) }.joinToString(" | "))
                .append(" => ").append((0 until r.length()).map { r.optString(it) }.joinToString(" || ")).append('\n')
        }
        return out.toString()
    }

    fun saveBrainFromText(text: String): List<String> {
        val rules = JSONArray()
        val errors = mutableListOf<String>()
        text.lines().forEachIndexed { index, raw ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed
            if (!line.contains("=>")) { errors.add("Ligne ${index+1}: il manque =>"); return@forEachIndexed }
            val parts = line.split("=>", limit = 2)
            val triggers = parts[0].split("|").map { it.trim() }.filter { it.isNotEmpty() }
            val replies = parts[1].split("||").map { it.trim() }.filter { it.isNotEmpty() }
            if (triggers.isEmpty()) errors.add("Ligne ${index+1}: aucun mot-clé.")
            else if (replies.isEmpty()) errors.add("Ligne ${index+1}: aucune réponse.")
            else {
                val t = JSONArray(); triggers.forEach { t.put(it) }
                val r = JSONArray(); replies.forEach { r.put(it) }
                rules.put(JSONObject().put("id", rules.length() + 1)
                    .put("nom", triggers.firstOrNull() ?: "Règle ${index+1}")
                    .put("active", true).put("declencheurs", t).put("reponses", r))
            }
        }
        if (errors.isEmpty()) {
            val brain = loadBrain()
            brain.put("regles", rules)
            saveBrain(brain)
        }
        return errors
    }

    /** JSON complet destiné à l'exportation. Inclut règles, paramètres et connaissances apprises. */
    fun exportBrainJson(): String = loadBrain().toString(2)

    /** Valide et importe un cerveau JSON sans écraser les données en cas d'erreur. */
    fun importBrainJson(jsonText: String): List<String> {
        return try {
            val candidate = JSONObject(jsonText)
            val rules = candidate.optJSONArray("regles")
                ?: return listOf("Le fichier ne contient pas de tableau 'regles'.")
            for (i in 0 until rules.length()) {
                val rule = rules.optJSONObject(i)
                    ?: return listOf("La règle ${i + 1} n'est pas un objet JSON valide.")
                val triggers = rule.optJSONArray("declencheurs")
                val replies = rule.optJSONArray("reponses")
                if (triggers == null || triggers.length() == 0)
                    return listOf("La règle ${i + 1} doit contenir au moins un déclencheur.")
                if (replies == null || replies.length() == 0)
                    return listOf("La règle ${i + 1} doit contenir au moins une réponse.")
                for (j in 0 until triggers.length()) if (triggers.optString(j).isBlank())
                    return listOf("Déclencheur vide dans la règle ${i + 1}.")
                for (j in 0 until replies.length()) if (replies.optString(j).isBlank())
                    return listOf("Réponse vide dans la règle ${i + 1}.")
                if (!rule.has("active")) rule.put("active", true)
                if (!rule.has("id")) rule.put("id", i + 1)
                if (!rule.has("nom")) rule.put("nom", "Règle ${i + 1}")
            }
            if (!candidate.has("version")) candidate.put("version", "3.1")
            if (!candidate.has("connaissances_apprises")) candidate.put("connaissances_apprises", JSONArray())
            if (!candidate.has("parametres")) candidate.put("parametres", defaultBrain().getJSONObject("parametres"))
            saveBrain(candidate)
            emptyList()
        } catch (e: Exception) {
            listOf("JSON illisible : ${e.message ?: "format invalide"}")
        }
    }

    private fun defaultBrain() = JSONObject()
        .put("version", "3.1")
        .put("regles", JSONArray())
        .put("connaissances_apprises", JSONArray())
        .put("parametres", JSONObject().put("similarite_minimum", 0.78)
            .put("apprentissage_gemini", true).put("conserver_reponses_gemini", true))
}
