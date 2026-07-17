# Stalker Player

Application Android (Kotlin + Jetpack Compose) permettant de se connecter à un
portail **Stalker Portal / Ministra** (le protocole utilisé par de nombreux
fournisseurs IPTV pour les box MAG), de parcourir les catégories et chaînes,
puis de lire le flux vidéo directement dans l'application.

## Fonctionnalités

- Enregistrement de plusieurs portails (URL + adresse MAC + identifiants optionnels)
- Connexion au portail : handshake, validation de profil (protocole Stalker/Ministra)
- Récupération des genres/catégories et de la liste des chaînes (pagination)
- Recherche de chaînes en temps réel
- Résolution du lien de lecture réel via `create_link`
- Lecture vidéo intégrée (HLS et flux progressifs) via Media3/ExoPlayer
- Interface moderne : thème sombre, dégradés, cartes arrondies, Material 3

## Comment ouvrir le projet

1. Ouvrez Android Studio (Koala ou plus récent recommandé).
2. `File > Open`, sélectionnez le dossier `StalkerPortalPlayer`.
3. Laissez Gradle synchroniser (le wrapper sera généré automatiquement si absent,
   sinon utilisez `gradle wrapper` en ligne de commande une fois avec une
   installation locale de Gradle 8.7).
4. Lancez l'application sur un émulateur ou un appareil (minSdk 24).

## Obtenir l'APK sans Android Studio : GitHub Actions

Le dossier `.github/workflows/build.yml` compile automatiquement un APK
"debug" prêt à installer, directement sur les serveurs de GitHub — vous
n'avez rien à installer sur votre machine.

Étapes :

1. Créez un nouveau dépôt sur [github.com/new](https://github.com/new)
   (public ou privé, peu importe).
2. Poussez le contenu de ce dossier dans le dépôt, par exemple :
   ```bash
   cd StalkerPortalPlayer
   git init
   git add .
   git commit -m "Premier commit"
   git branch -M main
   git remote add origin https://github.com/VOTRE-COMPTE/VOTRE-DEPOT.git
   git push -u origin main
   ```
   (Vous pouvez aussi le faire sans ligne de commande : sur GitHub, bouton
   "Add file > Upload files", et glissez tout le contenu du dossier.)
3. Sur la page de votre dépôt GitHub, ouvrez l'onglet **Actions**.
   Le workflow "Build APK" se lance automatiquement après le push
   (comptez 3 à 5 minutes).
4. Une fois terminé (coche verte), cliquez sur l'exécution du workflow,
   puis tout en bas dans **Artifacts**, téléchargez
   `StalkerPlayer-debug-apk` : c'est un zip contenant `app-debug.apk`.
5. Transférez cet APK sur votre téléphone Android (câble, email, Drive...)
   et installez-le en autorisant "Installer des applications inconnues"
   si Android le demande (l'APK n'étant pas signé par le Play Store).

Vous pouvez aussi relancer une compilation à tout moment sans rien changer
en cliquant sur "Run workflow" dans l'onglet Actions (grâce à
`workflow_dispatch`).

## Configuration d'un portail

Dans l'application, appuyez sur le bouton **+** et renseignez :

- **URL du portail** : l'adresse fournie par votre fournisseur IPTV
  (souvent sous la forme `http://domaine.tv/c/` ou `http://domaine.tv/stalker_portal/`).
- **Adresse MAC** : l'adresse MAC associée à votre abonnement.
- Identifiant / mot de passe si votre portail les requiert en plus de la MAC.

## Notes techniques importantes

- Le protocole Stalker Portal n'est pas un standard REST documenté
  publiquement ; les paramètres exacts de `get_profile` (version firmware,
  type de STB, etc.) varient légèrement selon les fournisseurs. Le client
  fourni (`StalkerPortalClient.kt`) utilise les valeurs les plus communément
  acceptées (émulation MAG250). Si un portail particulier refuse la
  connexion, il faudra ajuster ces paramètres dans `getProfile()`.
- L'application n'est prévue que pour se connecter à des portails auxquels
  vous disposez déjà d'un accès légitime (identifiants/MAC fournis par votre
  abonnement).
- `usesCleartextTraffic="true"` est activé car de nombreux portails Stalker
  fonctionnent encore en HTTP simple.

## Architecture du code

```
data/
  StalkerPortalClient.kt   -> logique du protocole (handshake, genres, chaînes, create_link)
  PortalRepository.kt      -> persistance des portails (DataStore)
  model/Models.kt          -> modèles de données
viewmodel/
  PortalsViewModel.kt      -> gestion de la liste des portails
  ChannelsViewModel.kt     -> connexion, genres, chaînes, recherche
ui/
  theme/                   -> thème Material 3 sombre "moderne"
  screens/                 -> PortalsScreen, ChannelsScreen, PlayerScreen
  navigation/NavGraph.kt   -> navigation Compose entre les écrans
```
