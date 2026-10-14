# ELMA BOT PRO 3.1 — Kotlin natif Android

Ce projet remplace Kivy/Python par Kotlin natif Android. Il contient l'interface, le cerveau local JSON, les thèmes, l'essai de réponses Gemini et un récepteur SMS Android.

## Compilation gratuite recommandée : GitHub Actions

1. Crée un dépôt GitHub et importe **tout le contenu** de ce dossier (pas le dossier ZIP lui-même).
2. Dans le dépôt, crée `.github/workflows/android.yml` avec le contenu indiqué dans `github-actions-workflow.yml`.
3. Ouvre l'onglet **Actions**, puis lance `Build ELMA BOT PRO APK`.
4. Une fois terminé, ouvre l'exécution et télécharge l'artefact `ELMA-BOT-PRO-debug-apk`.
5. Installe l'APK sur un téléphone Android. Android peut afficher un avertissement pour une application installée hors Play Store.

## Compilation locale

- Android Studio récent
- JDK 17
- Android SDK Platform 35
- Ouvre le dossier dans Android Studio, attends la synchronisation Gradle, puis **Build > Build APK(s)**.
- APK debug attendu : `app/build/outputs/apk/debug/app-debug.apk`

## Gemini

Dans l'application : **Paramètres > Configurer la clé Gemini**. La clé est conservée dans les préférences privées de l'application. Pour une application distribuée publiquement, ne mets pas une clé API partagée dans le client : un serveur intermédiaire est plus sûr.

## SMS — limites Android importantes

- Le projet demande `RECEIVE_SMS` et `SEND_SMS` à l'exécution.
- Android, les fabricants, les opérateurs et les règles Google Play peuvent limiter les SMS en arrière-plan.
- La réponse automatique ne traite que les règles locales; elle ne fait pas d'appel réseau à Gemini depuis le récepteur SMS.
- Le projet n'est pas configuré comme application SMS par défaut. Certaines fonctions SMS peuvent donc être limitées par le système.
- Teste uniquement avec des personnes qui savent que le répondeur automatique est actif, et surveille les frais SMS.

## Sécurité

Le mot de passe de démonstration du panneau de test est `Elma2026`, comme dans l'ancien code. Il est intégré à l'application et ne constitue pas une authentification forte. Change-le avant tout usage sensible.
