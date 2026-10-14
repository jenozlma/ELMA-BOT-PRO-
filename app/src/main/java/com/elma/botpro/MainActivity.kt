package com.elma.botpro

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var store: BrainStore
    private var themeName = "bleu"
    private var unlocked = false
    private val password = "Elma2026"
    private val themes = mapOf(
        "bleu" to intArrayOf(Color.rgb(7,14,31), Color.rgb(16,35,65), Color.rgb(20,140,255), Color.rgb(89,217,255), Color.WHITE, Color.rgb(148,174,205)),
        "violet" to intArrayOf(Color.rgb(19,9,33), Color.rgb(31,17,52), Color.rgb(148,71,255), Color.rgb(217,151,255), Color.WHITE, Color.rgb(180,160,202)),
        "vert" to intArrayOf(Color.rgb(5,26,21), Color.rgb(10,43,33), Color.rgb(20,198,122), Color.rgb(115,255,183), Color.WHITE, Color.rgb(150,205,178)),
        "rouge" to intArrayOf(Color.rgb(31,9,12), Color.rgb(48,15,20), Color.rgb(255,56,67), Color.rgb(255,151,105), Color.WHITE, Color.rgb(219,165,168)),
        "sombre" to intArrayOf(Color.rgb(5,5,7), Color.rgb(20,20,23), Color.rgb(125,135,154), Color.rgb(198,205,219), Color.WHITE, Color.rgb(155,157,166))
    )
    private val bg get() = themes[themeName]!![0]
    private val panel get() = themes[themeName]!![1]
    private val accent get() = themes[themeName]!![2]
    private val textColor get() = themes[themeName]!![4]
    private val muted get() = themes[themeName]!![5]
    private lateinit var root: LinearLayout
    private val requestExportJson = 7101
    private val requestImportJson = 7102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = BrainStore(this)
        themeName = store.loadConfig().optString("theme", "bleu").takeIf { themes.containsKey(it) } ?: "bleu"
        showHome()
    }

    private fun requestSmsPermissions() {
        val needed = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isNotEmpty()) ActivityCompat.requestPermissions(this, needed.toTypedArray(), 42)
    }

    private fun makeRoot(title: String): LinearLayout {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(18))
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
        root.addView(label(title, 25, textColor, true).apply { gravity = Gravity.CENTER_VERTICAL })
        root.addView(space(10))
        return root
    }

    private fun label(value: String, size: Int = 16, color: Int = textColor, bold: Boolean = false) =
        TextView(this).apply {
            text = value; textSize = size.toFloat(); setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(4), dp(7), dp(4), dp(7))
        }

    private fun space(h: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(h)) }

    private fun button(value: String, action: () -> Unit) = Button(this).apply {
        text = value; isAllCaps = false; setTextColor(Color.WHITE)
        background = rounded(accent, 14)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)).apply {
            bottomMargin = dp(9)
        }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }

    private fun iconButton(value: String, iconRes: Int, action: () -> Unit) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = rounded(panel, 18)
        val icon = ImageView(this@MainActivity).apply { setImageResource(iconRes); scaleType = ImageView.ScaleType.FIT_CENTER }
        addView(icon, LinearLayout.LayoutParams(dp(54), dp(54)))
        val text = TextView(this@MainActivity).apply {
            this.text = value; textSize = 16f; setTextColor(textColor); setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(14), 0, 0, 0)
        }
        addView(text, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        isClickable = true; isFocusable = true; setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(76)).apply { bottomMargin = dp(10) }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun showHome() {
        val r = makeRoot("ELMA BOT PRO")
        val logo = ImageView(this).apply { setImageResource(R.drawable.elma_logo); adjustViewBounds = true; scaleType = ImageView.ScaleType.FIT_CENTER }
        r.addView(logo, LinearLayout.LayoutParams(dp(150), dp(150)).apply { gravity = Gravity.CENTER; bottomMargin = dp(8) })
        r.addView(label("ASSISTANT INTELLIGENT • VERSION 3.1", 13, themes[themeName]!![3], true).apply { gravity = Gravity.CENTER })
        val brain = store.loadBrain()
        val rules = brain.optJSONArray("regles")
        var active = 0
        if (rules != null) for (i in 0 until rules.length()) if (rules.optJSONObject(i)?.optBoolean("active", true) == true) active++
        val card = TextView(this).apply {
            text = "ÉTAT DU SYSTÈME\n✓  Application ouverte\n🧠  $active règle(s) active(s)\nLes paramètres et règles sont conservés sur cet appareil."
            textSize = 15f; setTextColor(textColor); setPadding(dp(16), dp(16), dp(16), dp(16)); background = rounded(panel, 20)
        }
        r.addView(card); r.addView(space(14)); r.addView(label("MENU PRINCIPAL", 21, textColor, true))
        r.addView(iconButton("Cerveau ELMA\nRègles • mémoire • JSON", R.drawable.brain, ::showBrain))
        r.addView(iconButton("Mon Bot\nTester les réponses", R.drawable.bot, ::showBot))
        r.addView(iconButton("Console\nJournal des événements", R.drawable.console, ::showConsole))
        r.addView(iconButton("Gestion SMS\nAutorisations et état", R.drawable.sms, ::showSms))
        r.addView(iconButton("Paramètres\nThème • IA • comportement", R.drawable.settings, ::showSettings))
        r.addView(iconButton("Diagnostic\nÉtat des composants", R.drawable.diagnostic, ::showDiagnostic))
        r.addView(iconButton("Ouvrir un site web\nNavigateur Android", R.drawable.info, ::showBrowser))
        r.addView(iconButton("À propos\nVersion et informations", R.drawable.info, ::showInfo))
        r.addView(label("ELMA BOT PRO • Les fonctions SMS dépendent des autorisations Android.", 12, muted).apply { gravity = Gravity.CENTER })
    }

    private fun showBrain() {
        val r = makeRoot("🧠 CERVEAU ELMA")
        r.addView(label("Format : mot-clé | mot-clé => réponse || réponse", 13, muted))
        val editor = EditText(this).apply {
            setText(store.brainAsText()); minLines = 12; gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setTextColor(textColor); setHintTextColor(muted); setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(panel, 14)
        }
        r.addView(editor, LinearLayout.LayoutParams(-1, dp(340)))
        val status = label("", 13, muted)
        r.addView(button("💾 VALIDER", {
            val errors = store.saveBrainFromText(editor.text.toString())
            status.text = if (errors.isEmpty()) "✅ Cerveau enregistré." else "❌ " + errors.take(3).joinToString(" | ")
        }))
        r.addView(button("↻ RECHARGER", { editor.setText(store.brainAsText()); status.text = "Cerveau rechargé." }))
        r.addView(button("📥 IMPORTER UN CERVEAU JSON", { openImportJson() }))
        r.addView(button("📤 EXPORTER LE CERVEAU JSON", { openExportJson() }))
        r.addView(label("Chaque ligne : déclencheur | synonyme => réponse || autre réponse. Les règles importées en JSON sont conservées avec leurs paramètres et connaissances apprises.", 13, muted))
        r.addView(status)
        r.addView(button("← RETOUR", ::showHome))
    }

    private fun openImportJson() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, requestImportJson)
    }

    private fun openExportJson() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "elma_regles_cerveau.json")
        }
        startActivityForResult(intent, requestExportJson)
    }

    @Deprecated("Using activity result for compatibility with the existing project")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        try {
            when (requestCode) {
                requestImportJson -> {
                    val json = contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: throw IllegalStateException("Impossible de lire le fichier")
                    val errors = store.importBrainJson(json)
                    if (errors.isEmpty()) {
                        Toast.makeText(this, "Cerveau JSON importé et sauvegardé", Toast.LENGTH_LONG).show()
                        showBrain()
                    } else AlertDialog.Builder(this).setTitle("Import refusé").setMessage(errors.joinToString("\n")).setPositiveButton("OK", null).show()
                }
                requestExportJson -> {
                    contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(store.exportBrainJson()) }
                        ?: throw IllegalStateException("Impossible d'écrire le fichier")
                    Toast.makeText(this, "Cerveau exporté en JSON", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            AlertDialog.Builder(this).setTitle("Erreur fichier").setMessage(e.message ?: "Opération impossible").setPositiveButton("OK", null).show()
        }
    }

    private fun showBot() {
        val r = makeRoot("💬 RÉPONDEUR ELMA")
        val pass = EditText(this).apply {
            hint = "Mot de passe"; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(textColor); setHintTextColor(muted); background = rounded(panel, 12)
        }
        r.addView(pass)
        val question = EditText(this).apply {
            hint = "Écris un message à ELMA…"; minLines = 3; gravity = Gravity.TOP
            setTextColor(textColor); setHintTextColor(muted); background = rounded(panel, 12); isEnabled = false
        }
        r.addView(button("🔐 Déverrouiller", {
            unlocked = pass.text.toString() == password
            question.isEnabled = unlocked
            Toast.makeText(this, if (unlocked) "Accès autorisé" else "Mot de passe incorrect", Toast.LENGTH_SHORT).show()
        }))
        r.addView(question)
        val answer = label("ELMA attend ton message…", 16, textColor)
        r.addView(button("🤖 DEMANDER À ELMA", {
            if (!unlocked) { answer.text = "🔐 Déverrouille d’abord ELMA."; return@button }
            val msg = question.text.toString().trim()
            if (msg.isBlank()) { answer.text = "Écris un message."; return@button }
            answer.text = "🤖 ELMA réfléchit…"
            lifecycleScope.launch {
                answer.text = "🤖 ELMA réfléchit…"
                val local = store.findLocalAnswer(msg)
                if (local != null) {
                    answer.text = local
                } else {
                    val brain = store.loadBrain()
                    val params = brain.optJSONObject("parametres")
                    if (params?.optBoolean("apprentissage_gemini", true) != true) {
                        answer.text = "Aucune règle locale ne correspond. Active Gemini dans les paramètres ou ajoute une règle au cerveau."
                    } else {
                        try {
                            val cfg = store.loadConfig()
                            val result = GeminiClient.ask(cfg.optString("ai_key", ""), cfg.optString("model", "gemini-2.0-flash"), msg)
                            if (params.optBoolean("conserver_reponses_gemini", true)) store.saveLearnedAnswer(msg, result)
                            answer.text = result
                        } catch (e: Exception) {
                            answer.text = "⚠️ ${e.message ?: "Erreur pendant la demande Gemini."}"
                        }
                    }
                }
            }
        }))
        r.addView(answer)
        r.addView(button("← RETOUR", ::showHome))
    }

    private fun showConsole() {
        val r = makeRoot("CONSOLE ELMA")
        val brain = store.loadBrain(); val rules = brain.optJSONArray("regles")
        var active = 0; var total = rules?.length() ?: 0
        if (rules != null) for (i in 0 until rules.length()) if (rules.optJSONObject(i)?.optBoolean("active", true) == true) active++
        r.addView(label("● Journal local", 16, themes[themeName]!![3], true))
        r.addView(label("Application lancée\nCerveau lisible\nRègles actives : $active / $total\nStockage local : SharedPreferences", 15, textColor))
        r.addView(label("Les événements affichés ici sont des états connus par l'application ; ce n'est pas un journal système complet.", 13, muted))
        r.addView(button("Actualiser", ::showConsole)); r.addView(button("Exporter le cerveau JSON", ::openExportJson)); r.addView(button("← RETOUR", ::showHome))
    }

    private fun showSms() {
        val r = makeRoot("GESTION SMS")
        val receive = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val send = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        r.addView(label("État des autorisations", 17, themes[themeName]!![3], true))
        r.addView(label("Réception SMS : ${if (receive) "AUTORISÉE" else "NON AUTORISÉE"}\nEnvoi SMS : ${if (send) "AUTORISÉ" else "NON AUTORISÉ"}", 16, textColor))
        r.addView(label("Cette page affiche les permissions. La lecture de la boîte de réception et l'envoi interactif de SMS doivent être développés et testés séparément. Aucun message n'est inventé ou affiché ici.", 14, muted))
        r.addView(button("Demander les autorisations SMS", ::requestSmsPermissions))
        r.addView(button("Ouvrir les paramètres de l'application", { startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) }))
        r.addView(button("← RETOUR", ::showHome))
    }

    private fun showDiagnostic() {
        val r = makeRoot("DIAGNOSTIC")
        val brain = store.loadBrain(); val rules = brain.optJSONArray("regles")
        var active = 0; var total = rules?.length() ?: 0
        if (rules != null) for (i in 0 until rules.length()) if (rules.optJSONObject(i)?.optBoolean("active", true) == true) active++
        val cfg = store.loadConfig()
        r.addView(label("VÉRIFICATIONS LOCALES", 17, themes[themeName]!![3], true))
        r.addView(label("✓ Stockage des paramètres accessible\n✓ Fichier cerveau lisible\n✓ JSON chargé\n$active / $total règle(s) active(s)\n${if (cfg.optString("ai_key").isNotBlank()) "Clé Gemini configurée" else "Clé Gemini non configurée"}\nRéception SMS : ${if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) "autorisée" else "non autorisée"}", 15, textColor))
        r.addView(label("Ce diagnostic vérifie des éléments locaux simples. Il ne remplace pas une compilation et un test sur appareil.", 13, muted))
        r.addView(button("Relancer le diagnostic", ::showDiagnostic)); r.addView(button("Ouvrir la console", ::showConsole)); r.addView(button("← RETOUR", ::showHome))
    }

    private fun showSettings() {
        val r = makeRoot("⚙️ PARAMÈTRES")
        r.addView(label("🎨 Choisis la couleur d’ELMA", 16, muted))
        listOf("bleu" to "🔵 Bleu ELMA", "violet" to "🟣 Violet", "vert" to "🟢 Vert", "rouge" to "🔴 Rouge", "sombre" to "🌑 Sombre").forEach { (key, title) ->
            r.addView(button(title) {
                themeName = key
                val cfg = store.loadConfig().put("theme", key)
                store.saveConfig(cfg)
                showSettings()
            })
        }
        val brain = store.loadBrain()
        val params = brain.optJSONObject("parametres") ?: JSONObject().also { brain.put("parametres", it) }
        r.addView(button("🧠 Apprentissage Gemini : " + if (params.optBoolean("apprentissage_gemini", true)) "ON" else "OFF") {
            params.put("apprentissage_gemini", !params.optBoolean("apprentissage_gemini", true))
            store.saveBrain(brain); showSettings()
        })
        val cfg = store.loadConfig()

        // Champ Gemini visible directement dans Paramètres, sans supprimer les options existantes.
        r.addView(label("✨ CONFIGURATION GEMINI", 18, textColor, true))
        r.addView(label("Colle ta clé API ici. Elle sera enregistrée sur cet appareil.", 13, muted))
        val geminiKeyInput = EditText(this).apply {
            hint = "Clé API Gemini"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(cfg.optString("ai_key", ""))
            setTextColor(textColor)
            setHintTextColor(muted)
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(panel, 12)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply {
                bottomMargin = dp(9)
            }
        }
        r.addView(geminiKeyInput)
        r.addView(button("💾 Enregistrer la clé Gemini") {
            val value = geminiKeyInput.text.toString().trim()
            store.saveConfig(store.loadConfig().put("ai_key", value).put("ai_provider", "GEMINI"))
            Toast.makeText(this, if (value.isBlank()) "Clé vide : Gemini ne pourra pas répondre" else "Clé Gemini enregistrée", Toast.LENGTH_SHORT).show()
            showSettings()
        })
        r.addView(button("🧪 Tester la connexion Gemini") {
            val key = geminiKeyInput.text.toString().trim()
            if (key.isBlank()) {
                Toast.makeText(this, "Colle d’abord ta clé API Gemini.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Test Gemini en cours…", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    try {
                        val model = store.loadConfig().optString("model", "gemini-2.0-flash")
                        val result = GeminiClient.ask(key, model, "Réponds uniquement : OK")
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Gemini fonctionne")
                            .setMessage("Connexion réussie. Réponse : $result")
                            .setPositiveButton("OK", null).show()
                    } catch (e: Exception) {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Échec du test Gemini")
                            .setMessage(e.message ?: "Vérifie la clé API et la connexion Internet.")
                            .setPositiveButton("OK", null).show()
                    }
                }
            }
        })
        r.addView(space(8))
        r.addView(label("🧠 Comportement du cerveau", 16, muted, true))
        r.addView(button("Seuil de correspondance : ${((brain.optJSONObject("parametres")?.optDouble("similarite_minimum", 0.78) ?: 0.78) * 100).toInt()}%") {
            val params = brain.optJSONObject("parametres") ?: JSONObject().also { brain.put("parametres", it) }
            val seek = SeekBar(this).apply { max = 50; progress = (((params.optDouble("similarite_minimum", 0.78) - 0.5) / 0.5) * 50).toInt().coerceIn(0,50) }
            AlertDialog.Builder(this).setTitle("Précision des déclencheurs").setMessage("Plus le seuil est élevé, plus la correspondance doit être proche.").setView(seek)
                .setPositiveButton("Enregistrer") { _, _ -> params.put("similarite_minimum", 0.5 + seek.progress / 100.0); store.saveBrain(brain); showSettings() }
                .setNegativeButton("Annuler", null).show()
        })
        r.addView(button("Conserver les réponses Gemini : ${if (brain.optJSONObject("parametres")?.optBoolean("conserver_reponses_gemini", true) != false) "OUI" else "NON"}") {
            val params = brain.optJSONObject("parametres") ?: JSONObject().also { brain.put("parametres", it) }
            params.put("conserver_reponses_gemini", !params.optBoolean("conserver_reponses_gemini", true)); store.saveBrain(brain); showSettings()
        })
        r.addView(button("Choisir le modèle Gemini") {
            val input = EditText(this).apply { setText(cfg.optString("model", "gemini-2.0-flash")); hint = "Nom du modèle" }
            AlertDialog.Builder(this).setTitle("Modèle IA").setView(input).setPositiveButton("Enregistrer") { _, _ -> store.saveConfig(store.loadConfig().put("model", input.text.toString().trim().ifBlank { "gemini-2.0-flash" })); showSettings() }.setNegativeButton("Annuler", null).show()
        })
        r.addView(button("Effacer la clé Gemini", {
            AlertDialog.Builder(this).setTitle("Effacer la clé ?").setMessage("Les réponses locales continueront de fonctionner.").setPositiveButton("Effacer") { _, _ -> store.saveConfig(store.loadConfig().put("ai_key", "")); showSettings() }.setNegativeButton("Annuler", null).show()
        }))
        r.addView(button("Exporter les règles et la mémoire", ::openExportJson))
        r.addView(label("Les thèmes, la clé IA, le modèle et les paramètres du cerveau sont enregistrés localement.", 13, muted))
        r.addView(button("🔑 Configurer la clé Gemini", {
            val current = store.loadConfig().optString("ai_key", "")
            val input = EditText(this).apply {
                hint = "Colle ici la clé API Gemini"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setText(current)
                setTextColor(textColor); setHintTextColor(muted); background = rounded(panel, 12)
            }
            AlertDialog.Builder(this).setTitle("Clé API Gemini").setMessage("La clé est enregistrée dans les paramètres privés de cette installation.") .setView(input)
                .setPositiveButton("Enregistrer") { _, _ ->
                    val value = input.text.toString().trim()
                    store.saveConfig(store.loadConfig().put("ai_key", value).put("ai_provider", "GEMINI"))
                    Toast.makeText(this, if (value.isBlank()) "Aucune clé enregistrée" else "Clé Gemini enregistrée", Toast.LENGTH_SHORT).show()
                    showSettings()
                }.setNegativeButton("Annuler", null).show()
        }))
        r.addView(button("🌐 Ouvrir un lien dans le navigateur", ::showBrowser))
        r.addView(button("← RETOUR", ::showHome))
    }

    private fun showBrowser() {
        val input = EditText(this).apply {
            hint = "https://example.com"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setTextColor(textColor); setHintTextColor(muted); background = rounded(panel, 12)
        }
        AlertDialog.Builder(this).setTitle("Ouvrir un site web")
            .setMessage("Saisis une adresse web. Le navigateur Android par défaut sera utilisé.")
            .setView(input)
            .setPositiveButton("Ouvrir") { _, _ ->
                var address = input.text.toString().trim()
                if (address.isNotBlank()) {
                    if (!address.startsWith("https://", true) && !address.startsWith("http://", true)) address = "https://$address"
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(address)))
                    } catch (_: Exception) {
                        Toast.makeText(this, "Aucune application ne peut ouvrir ce lien.", Toast.LENGTH_LONG).show()
                    }
                }
            }.setNegativeButton("Annuler", null).show()
    }

    private fun showInfo() {
        val r = makeRoot("ℹ️ ELMA BOT PRO")
        r.addView(label("🤖 Répondeur intelligent\n🧠 Cerveau modifiable\n✨ Réponses Gemini (clé API requise)\n📱 Réponse automatique SMS pour les règles locales\n🎨 Thèmes personnalisables\n\nCréateur : Monsieur Kiomba Somwe Jenovic Elma\nCréé le : 29/08/2026", 16, textColor))
        r.addView(label("Note : l'envoi/réception de SMS dépend des autorisations Android, de l'opérateur et des règles du système.", 13, muted))
        r.addView(button("← RETOUR", ::showHome))
    }
}
