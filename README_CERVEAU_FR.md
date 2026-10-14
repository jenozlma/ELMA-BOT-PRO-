# ELMA BOT PRO — Cerveau modifiable (JSON + éditeur intégré)

## Où trouver les fonctions
Ouvrir **ELMA BOT PRO → Cerveau ELMA**.

- Éditeur intégré : écrire les règles puis toucher **VALIDER**.
- Importer un cerveau JSON : choisir un fichier `.json` avec le sélecteur Android.
- Exporter le cerveau JSON : choisir où enregistrer `elma_regles_cerveau.json`.
- RECHARGER : recharger la version persistée dans l'application.

Les règles et les connaissances apprises sont sauvegardées dans les préférences privées de l'application. Elles restent disponibles après fermeture et réouverture. Effacer les données ou désinstaller l'application efface cette copie locale : exporter régulièrement une sauvegarde si elle est importante.

## Format texte de l'éditeur
Une règle par ligne :

```text
# Les lignes qui commencent par # sont des commentaires
salut | bonjour | slt => Bonjour ! || Salut, comment vas-tu ?
qui es-tu | tu es qui => Je suis ELMA BOT PRO.
```

- `|` sépare les déclencheurs/synonymes.
- `=>` sépare les déclencheurs des réponses.
- `||` sépare les réponses possibles.
- Une ligne vide ou commentaire est ignorée.
- En cas d'erreur, le cerveau sauvegardé n'est pas remplacé.
- La validation texte remplace la liste des règles, mais préserve les paramètres et les connaissances apprises.

## Format JSON
Le schéma utilisé par le moteur est celui-ci :

```json
{
  "version": "3.1",
  "regles": [
    {
      "id": 1,
      "nom": "Salutations",
      "active": true,
      "declencheurs": ["salut", "bonjour", "slt"],
      "reponses": ["Bonjour !", "Salut, comment vas-tu ?"]
    }
  ],
  "connaissances_apprises": [],
  "parametres": {
    "similarite_minimum": 0.78,
    "apprentissage_gemini": true,
    "conserver_reponses_gemini": true
  }
}
```

`active` vaut `true` si absent. Une règle inactive est ignorée par le moteur local. `id` et `nom` sont des métadonnées utiles à l'interface. L'import vérifie la structure et refuse le fichier sans remplacer le cerveau existant en cas d'erreur. Un JSON valide remplace la configuration du cerveau actuelle; exportez d'abord si vous souhaitez la conserver.

## Comment une règle est appliquée
1. Le moteur normalise la phrase (minuscules, accents et ponctuation).
2. Il compare le message aux déclencheurs des règles actives et aux connaissances apprises.
3. Si le meilleur score atteint `parametres.similarite_minimum`, une réponse locale est choisie.
4. Sinon, l'écran de test peut appeler Gemini si une clé est configurée et si `apprentissage_gemini` est activé.
5. Selon `conserver_reponses_gemini`, la réponse Gemini peut être enregistrée dans les connaissances apprises.

## Limites / sécurité
- Le JSON est du contenu de données : il ne peut pas exécuter du code Kotlin/Python.
- Les règles ne donnent pas à ELMA des capacités qui n'existent pas dans l'application; elles contrôlent principalement les réponses textuelles.
- Les fonctions SMS dépendent des permissions et des restrictions Android. Testez avec des messages non sensibles avant activation réelle.
- La clé Gemini est stockée dans les préférences privées de l'application, mais cette version ne fournit pas de coffre-fort matériel; ne partagez pas un export de configuration contenant des secrets. Le fichier exporté par cette fonctionnalité contient le cerveau, pas la clé API.

## État de vérification
Les sources et le ZIP ont été modifiés et l'archive a été vérifiée. Aucune compilation Android réelle n'a été effectuée dans cet environnement; il faut lancer une compilation Gradle valide et installer l'APK sur un téléphone de test avant de considérer la version comme prête à publier.
