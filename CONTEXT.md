# CONTEXTE PROJET — AGE OF MAYANE

> Fichier de contexte pour les assistants IA (Claude, ChatGPT, etc.)
> À mettre à jour à chaque grande étape.
> Toujours coller ce contenu au début d'une nouvelle conversation.

---

## 1. INFOS PROJET

- **Nom du jeu** : Age of Mayane
- **Type** : Twin-stick shooter top-down rogue-like
- **Framework** : LibGDX 1.12.1
- **Langage** : Java 17
- **Package** : com.ageofmayane.game
- **Repo** : github.com/gborisl94/ageofmayane
- **Branches actives** :
  - `main` : version stable de référence
  - `V5` : dernière version fonctionnelle (rogue-like + textures)
  - `V6` : version en développement (actuellement en cours)
- **Build** : GitHub Actions (workflow `.github/workflows/build-apk.yml`)
- **Dernière mise à jour** : 11/10/2025

---

## 2. MATÉRIEL CIBLE

- **Téléphone de test** : TECNO POP 8 (BG6)
- **Android** : 13 (API 33)
- **CPU** : Unisoc T606
- **GPU** : Mali-G57
- **RAM** : 3 GB
- **Contrainte** : OpenGL ES 2.0 UNIQUEMENT (useGL30 = false)

---

## 3. CONTRAINTES DE DÉVELOPPEMENT

### Je code depuis un téléphone Android
- Pas de PC, pas de terminal, pas d'IDE
- Copie-colle dans GitHub Web
- Les fichiers > 200 lignes passent mal → découper en morceaux de 150 lignes max

### Je ne peux pas lire les logs Android
- Pas d'ADB, pas de Logcat Reader
- Je ne vois une erreur QUE si le build GitHub Actions échoue
- Le code doit être TRÈS robuste : try/catch partout, fallbacks partout

### Je travaille avec plusieurs assistants IA
- Chaque fichier a **UNE SEULE source de vérité**
- Si un fichier vient de Claude, je ne le modifie pas avec un autre assistant
- Je tiens à jour ce fichier CONTEXT.md pour le contexte partagé

---

## 4. ARCHITECTURE DES FICHIERS

### Code Java