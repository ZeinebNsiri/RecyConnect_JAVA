# ♻️ RecyConnect – Application Java de gestion du recyclage

[![forthebadge](https://forthebadge.com/images/badges/built-with-love.svg)](https://forthebadge.com) 
[![forthebadge](https://forthebadge.com/images/badges/powered-by-electricity.svg)](https://forthebadge.com)

**RecyConnect** est une application Java/JavaFX axée sur le développement durable. Elle vise à faciliter les échanges entre producteurs de déchets (supermarchés, restaurants, particuliers) et les entités de valorisation (usines, associations, citoyens engagés). Le projet s’inscrit pleinement dans une démarche éco-responsable et contribue aux Objectifs de Développement Durable (ODD).

---

## ✨ Fonctionnalités principales

- 🔐 **Gestion des utilisateurs**
  - Création de compte avec reconnaissance faciale
  - Authentification sécurisée
  - Activation/désactivation de comptes par l'administrateur

- 📦 **Gestion des articles**
  - Ajout, modification, suppression et catégorisation des articles
  - Modération des images avec API Sightengine
  - Notification par email en cas de refus

- 🛒 **Gestion des commandes**
  - Ajout au panier, modification des quantités, suppression
  - Paiement via Paymee ou à la livraison
  - Génération de factures PDF

- 📝 **Gestion des posts (Forum)**
  - Création de publications avec médias
  - Filtrage par tags
  - Likes, commentaires, réponses imbriquées
  - Validation de contenu avec Gemini API

- 📆 **Gestion des événements**
  - Création, modification et suppression d'événements (en ligne ou sur site)
  - Intégration avec Jitsi Meet pour les visioconférences
  - Affichage de la carte avec Leaflet et géolocalisation via Nominatim

- 🧪 **Gestion des workshops**
  - Ajout, modification, suppression de workshops
  - Attribution de notes (1 à 5), filtrage, affichage des moyennes
  - Statistiques par catégorie, chatbot intégré, vidéo explicative
  - Génération automatique de description via analyse vidéo (IA)

---

## 🧰 Technologies utilisées

### 👨‍💻 Backend & Frontend
- **Java 17**
- **JavaFX** – Interface utilisateur
- **JDBC** – Connexion base de données
- **MySQL** – Stockage des données

### 🔌 APIs & Intégrations externes
- **[Face++ API](https://www.faceplusplus.com/)** – Reconnaissance faciale
- **[Paymee](https://sandbox.paymee.tn/)** – Paiement sécurisé
- **[Sightengine](https://sightengine.com/)** – Détection de contenu inapproprié
- **[Mailer API](https://www.mailersend.com/)** – Notification par email
- **[Gemini API (Google)](https://deepmind.google/technologies/gemini/)** – Analyse sémantique des contenus
- **[Leaflet](https://leafletjs.com/)** – Cartographie interactive
- **[MapTiler Cloud](https://www.maptiler.com/cloud/)** – Amélioration des cartes
- **[Jitsi Meet](https://jitsi.org/)** – Génération de visioconférences

---

## 🏁 Lancement du projet

1. **Cloner le dépôt**
   ```bash
   git clone https://github.com/ZeinebNsiri/RecyConnect_JAVA.git
