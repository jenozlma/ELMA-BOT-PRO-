# ELMA BOT PRO — projet Android complet (sources)

## Ce que contient cette archive
- Projet Android natif Kotlin/Gradle (ce n'est pas la variante Kivy).
- Accueil bleu nuit / cyan avec logo PNG et navigation par boutons illustrés.
- Cerveau ELMA : édition de règles texte, import JSON, export JSON, validation et sauvegarde locale persistante.
- Moteur local de règles : déclencheurs, réponses multiples, règles activées/désactivées, correspondance approximative et connaissances apprises.
- Répondeur de test local, avec option Gemini lorsque la clé API est configurée.
- Paramètres : thèmes, seuil de correspondance, conservation des réponses Gemini, modèle IA, gestion de la clé API et export du cerveau.
- Console informative, écran de permissions SMS, diagnostic local et informations.
- Images PNG incluses et catalogue détaillé dans `ASSETS_PNG_FR.md`.
- Exemple JSON dans `EXEMPLE_CERVEAU.json`.
- Workflow GitHub Actions dans `github-actions-workflow.yml`.

## Compilation Android
1. Extraire l'archive.
2. Ouvrir le dossier du projet dans Android Studio, avec JDK 17 et Android SDK 35.
3. Laisser Gradle synchroniser les dépendances.
4. Lancer `assembleDebug` ou utiliser le workflow GitHub Actions.
5. Tester l'APK sur un appareil Android réel et lire Logcat en cas de problème.

Le fichier `gradlew` hérité de l'archive de départ était vide ; la compilation locale doit donc être lancée depuis Android Studio ou avec Gradle installé, ou via GitHub Actions. Le workflow utilise Gradle 8.9.

## Important — état des tests
Le ZIP a fait l'objet de vérifications statiques : structure des fichiers, présence des PNG, validation JSON de l'exemple et présence des références de ressources. Un SDK Android/Gradle utilisable n'est pas installé dans l'environnement de préparation ; **aucune compilation APK ni exécution sur téléphone n'a été effectuée ici**. Il serait donc incorrect de garantir qu'il n'existe aucune erreur de compilation ou d'exécution avant le test Android.

## Fonctionnalités et limites honnêtes
- Les règles locales s'appliquent aux messages envoyés au moteur de test du bot.
- L'écran SMS affiche l'état des autorisations ; la lecture complète de la boîte SMS et un flux de réponse automatique ne sont pas présentés comme terminés ou testés.
- La réception automatique dépend du système Android, des permissions et des restrictions constructeur.
- Gemini nécessite une clé API valide et une connexion Internet. Ne partagez pas l'archive si elle contient une clé personnelle.
- Le code actuel contient encore un verrou de démonstration simple dans l'écran de test du répondeur ; il ne s'agit pas d'une protection de sécurité de niveau production.

## Formats des règles
Éditeur texte :
`salut | bonjour | slt => Bonjour ! || Salut, comment vas-tu ?`

Format JSON : tableau `regles` avec `id`, `nom`, `active`, `declencheurs` et `reponses`. Exemple dans `EXEMPLE_CERVEAU.json`.

## Catalogue des images
Voir `ASSETS_PNG_FR.md` pour le nom et la fonction de chaque PNG inclus.
