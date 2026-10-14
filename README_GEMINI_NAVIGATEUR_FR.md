# ELMA BOT PRO — correctifs Gemini et navigateur (version 1)

## Modifications

1. `BrainStore.kt` charge `vic_config.json` depuis les assets comme configuration initiale, fusionne les paramètres enregistrés et accepte les noms `ai_key`, `gemini_api_key`, `api_key` et `gemini.api_key` / `gemini.key`.
2. `GeminiClient.kt` envoie une requête HTTPS `generateContent`, lit plusieurs parties de texte de la réponse et remonte les erreurs HTTP/réseau pour qu'elles soient visibles dans l'interface.
3. `MainActivity.kt` affiche les réponses Gemini dans l'écran du bot, ne mémorise que les réponses réussies, préremplit la clé configurée et permet de la modifier depuis Paramètres.
4. Le bouton « Ouvrir un site web » utilise `Intent.ACTION_VIEW` pour déléguer le lien au navigateur ou à l'application Android compatible.
5. `AndroidManifest.xml` possède déjà la permission `INTERNET`, nécessaire à Gemini. Aucun secret réel n'est inclus dans l'archive.

## Configurer Gemini

- Ouvrir Paramètres → Configurer la clé Gemini.
- Coller la clé API créée dans Google AI Studio et appuyer sur Enregistrer.
- Vérifier que le téléphone a accès à Internet.
- Ouvrir Mon Bot, déverrouiller l'écran, écrire une question puis appuyer sur « DEMANDER À ELMA ».
- Les erreurs de clé, de modèle, de réseau ou de quota sont désormais présentées dans l'écran au lieu d'être silencieusement remplacées par une réponse vide.

`app/src/main/assets/vic_config.json` sert de configuration initiale intégrée à l'application. Un asset est en lecture seule après installation : pour modifier la clé sans recompiler, utiliser l'écran Paramètres. Pour la sécurité, ne publiez pas une clé API dans un dépôt public ou dans un APK partagé.

## Navigateur

Depuis Accueil ou Paramètres, appuyer sur « Ouvrir un site web », saisir par exemple `https://www.google.com`, puis Ouvrir. Android lance l'application par défaut capable de traiter ce lien.

## Vérifications et limites

- Le ZIP et la structure des fichiers peuvent être vérifiés localement.
- La compilation Android n'a pas été exécutée ici : l'archive source fournie ne contient pas un wrapper Gradle complet (`gradlew` est un petit lanceur de remplacement et `gradle-wrapper.jar` est absent), et Gradle n'est pas installé dans cet environnement.
- Ouvrir le dossier dans Android Studio, synchroniser Gradle, puis compiler `app` pour confirmer la compatibilité avec l'environnement Android ciblé.
- Une vraie clé Gemini, Internet et un modèle encore disponible sur le compte sont nécessaires pour obtenir une réponse de l'API.
