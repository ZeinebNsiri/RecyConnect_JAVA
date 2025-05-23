
# <img src="mainlogo.png" alt="RecyConnect Logo" width="80" height="80" style="vertical-align:middle;"> **RecyConnect** – *Application Java de gestion du recyclage*

![Recyclé](https://img.shields.io/badge/Made%20with-Recycled%20Materials-4CAF50?style=flat&logo=recycle&logoColor=white&labelColor=1B5E20)
![Éco-responsable](https://img.shields.io/badge/Environment-Éco--responsable-2E7D32?style=flat&logo=leaf&logoColor=white&labelColor=004D40)


**RecyConnect** est une application Java/JavaFX axée sur le développement durable et l’intelligence artificielle (IA). Elle vise à faciliter les échanges entre producteurs de déchets (supermarchés, restaurants, particuliers) et les entités de valorisation (usines, associations, citoyens engagés). Le projet s’inscrit pleinement dans une démarche éco-responsable et contribue aux Objectifs de Développement Durable (ODD).

---
## 🎯 Objectifs de Développement Durable (ODD)

RecyConnect s’inscrit pleinement dans une démarche éco-responsable en répondant à plusieurs **Objectifs de Développement Durable (ODD)** définis par les Nations Unies :

<table>
  <tr>
    <td align="center">
      <img src="https://sdgs.un.org/sites/default/files/goals/E_SDG_Icons-08.jpg" width="80"/><br/>
      <strong>ODD 8</strong><br/>
      Travail décent et croissance économique
    </td>
    <td align="center">
      <img src="https://sdgs.un.org/sites/default/files/goals/E_SDG_Icons-11.jpg" width="80"/><br/>
      <strong>ODD 11</strong><br/>
      Villes et communautés durables
    </td>
    <td align="center">
      <img src="https://sdgs.un.org/sites/default/files/goals/E_SDG_Icons-12.jpg" width="80"/><br/>
      <strong>ODD 12</strong><br/>
      Consommation et production responsables
    </td>
    <td align="center">
      <img src="https://sdgs.un.org/sites/default/files/goals/E_SDG_Icons-13.jpg" width="80"/><br/>
      <strong>ODD 13</strong><br/>
      Lutte contre le changement climatique
    </td>
  </tr>
</table>

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
- **[**Chatbot intégré**](https://huggingface.co/)** Interaction automatisée avec les utilisateurs pour l’assistance ou les réponses fréquentes.
- **[**Analyse Vidéo**](https://huggingface.co/)** Analyse vidéo et géneration du description automatique    
- **[Mailer API](https://www.mailersend.com/)** – Notification par email
- **[Gemini API (Google)](https://deepmind.google/technologies/gemini/)** – Analyse sémantique des contenus
- **[Leaflet](https://leafletjs.com/)** – Cartographie interactive
- **[MapTiler Cloud](https://www.maptiler.com/cloud/)** – Amélioration des cartes
- **[Jitsi Meet](https://jitsi.org/)** – Génération de visioconférences

### 🧠 Intelligence Artificielle & IA appliquée
- **Gemini API (Google)** – Analyse sémantique, validation de contenu
- **Analyse vidéo par IA** – Génération automatique de résumés pour les workshops
- **Chatbot intelligent** – Réponses automatiques aux questions fréquentes
- **Event recommandation** – Recommandation d’événements à partir du traitement du langage naturel  
- **Event Image Generation from Title and Desc** – Génération d’images d’événement à partir du titre et de la description  
- **Reconnaissance Faciale** – Identification des visages à partir d’images ou de vidéos  
- **Détection Mauvaises Images** – Filtrage automatique des images inappropriées ou non conformes  
- **Analyse Vidéo to Text** – Transcription automatique du contenu vidéo en texte  

---

## 📸 Aperçu de l’application
Voici quelques captures d’écran de l’interface utilisateur de RecyConnect :

### 🖼️ Accueil de l'application
<p align="center">
<img src="java1.jfif" alt="Accueil RecyConnect" width="600"/>
</p>

### 🛍️ Interface de gestion des articles
<p align="center">
<img src="java2.jfif" alt="Gestion des articles RecyConnect" width="600"/>
</p>
---

## 📚 Projet académique

Ce projet a été réalisé dans le cadre d’un projet académique à l’école d’ingénierie **ESPRIT** (École Supérieure Privée d'Ingénierie et de Technologies), démontrant la capacité à intégrer des technologies avancées telles que **JavaFX**, des **API d’IA** et des outils de **développement durable** dans une solution complète.

---

## 👥 Équipe projet – TechSquad

Ce projet a été réalisé dans le cadre d’un projet académique à l’école **ESPRIT**, par un groupe de 6 étudiantes en ingénierie informatique, passionnées par l’innovation durable et l’intelligence artificielle.

**Membres de l'équipe : TechSquad**
- Sahar Mnif  
- Zeineb Nsiri  
- Mohamed Aziz Zouari
- Samar Touil
- Amal Eljazi
- Eya Guirat

Nous avons collaboré sur toutes les étapes du projet : conception, développement, intégration d'API et documentation. Ce travail reflète notre engagement pour un avenir plus vert et plus intelligent 🌍🤖.


## 🏁 Lancement du projet

1. **Cloner le dépôt**
   ```bash
   git clone https://github.com/ZeinebNsiri/RecyConnect_JAVA.git
   ```

---
