Bee Board \- Memory

**Glossaire \- Ubiquitous Language \- Domain Driven Design**

*Ce glossaire sert à la définition de l’Ubiquitous Language et n’est pas destiné à être mis à jour par la suite.*

---

#### **Note**

Définition : Une note est un texte contenant des informations à partager sur divers sujets (ex. : astuces, raccourcis clavier, règles métier, formules de calcul, annonces technologiques, recommandations culturelles).

Caractéristiques :

* Peut contenir du texte, des images, du code.  
* Peut être partagée avec un ou plusieurs développeurs ou toute l’équipe.  
* Peut être classée par projet, langage de programmation ou type de problème.  
* Peut contenir des étiquettes (ex. : "bug", "optimisation", "documentation").  
* Peut être commentée et donner lieu à des discussions.

---

#### 

#### **Étiquette**

Définition : Un mot-clé ou une catégorie permettant de filtrer et organiser les notes plus efficacement.

Exemples : "Bug", "Documentation", "Optimisation", "Refactoring", "Urgent".

---

#### **Catégorie**

Définition : Classification structurée permettant d’identifier rapidement la nature d’une note en fonction du problème ou du sujet qu’elle traite. Contrairement aux étiquettes, qui sont des mots-clés libres définis par les utilisateurs, une catégorie est une classification prédéfinie qui structure l’organisation des notes dans l’application.

Différence avec une Étiquette :

* Catégorie \= Classification stricte et prédéfinie (une seule par note).  
* Étiquette \= Mots-clés flexibles et multiples permettant de mieux organiser et filtrer les notes.  
* Une note doit avoir une catégorie, mais peut avoir plusieurs étiquettes.

---

#### **Tâche**

Définition : Une tâche est une unité de travail attribuée à un développeur. Elle peut varier en importance et en urgence. Une tâche est liée à un projet. 

Différence avec une Note : Une tâche implique une action à réaliser, tandis qu’une note est purement informative.

Assignation : Une tâche doit être explicitement assignée à un utilisateur.

Lien avec les Notes : Une tâche n’est pas une note, mais les notes peuvent enrichir les informations relatives à une tâche.

---

#### 

#### **Tableau de Bord**

Définition : Interface personnalisée pour chaque développeur affichant les éléments clés de son activité.

Contenu :

* Notes récentes  
* Tâches assignées  
* Projets en cours  
* Discussions actives  
* Liste des contributeurs partageant des projets communs

---

#### 

#### **Développeur**

Définition : Un membre de l’équipe technique travaillant sur les différents projets et utilisant le système de gestion de notes.

---

#### **Responsable des Ressources Humaines (RH)**

Définition : Personne chargée de la gestion des talents, du recrutement et du suivi des compétences des développeurs au sein de l’entreprise.

Rôle dans l’application :

* Accès aux profils des développeurs et à leurs compétences.  
* Possibilité d’analyser les métriques de participation.  
* Peut utiliser l’outil pour évaluer la collaboration et l’évolution des équipes.

---

#### **Chef de Projet (Project Manager)**

Définition : Responsable de la coordination des projets, garant de leur avancement et du respect des délais.

Rôle dans l’application :

* Peut consulter les notes et les discussions pour suivre l’évolution des projets.  
* Responsable de la gestion des tâches et de leur répartition.  
* Fait le lien entre les développeurs et la direction.  
* Peut générer des rapports de participation et suivre les contributions des équipes.

---

### **Direction**

Définition : Ensemble des responsables en charge de la gestion stratégique et organisationnelle de l’entreprise. Dans le cadre de l’application, la direction supervise les équipes et veille à l’alignement des projets avec les objectifs de l’entreprise.

Rôle dans l’Application :

* A une vue d’ensemble sur les échanges et les contributions des équipes​.  
* Consulte les rapports de participation et les métriques de productivité​.  
* Ne participe pas activement aux discussions techniques mais peut avoir un rôle d’observateur​.

---

#### **Projet**

Définition : Un ensemble d’activités organisées autour d’un objectif commun, auquel les développeurs sont assignés.

Lien avec les Notes : Chaque note peut être liée à un ou plusieurs projets.

Lien avec les Tâches : Les tâches sont attribuées à des projets.

---

### **Équipe**

Définition : Groupe de professionnels travaillant sur les projets de l’entreprise, répartis selon leurs compétences et responsabilités.

equipe \= ensemble des contributeurs pour un projet

---

### **Discussion**

Définition : Échange structuré entre membres de l’équipe autour d’une note, permettant le partage d’idées, la résolution de problèmes ou la prise de décision.

Caractéristiques :

* Chaque note peut être commentée pour permettre un échange asynchrone​.  
* Les discussions sont organisées sous forme de fils de conversation, permettant un suivi clair​.  
* Notifications en cas de nouveaux commentaires sur une note suivie.

---

### **Commentaire**

Définition : Message textuel ajouté à une note pour échanger des informations, poser des questions ou proposer des solutions.

Caractéristiques :

* Associé à une note et permet d’ouvrir une discussion​.  
* Peut être utilisé pour compléter une information, signaler un problème ou donner un avis.  
* Ils s'organisent sous forme de fils de discussion, afin de structurer les échanges​.  
* Notifications envoyées aux personnes concernées lorsqu’un nouveau commentaire est ajouté​.

---

### 

### **Solution**

Définition : Réponse ou action permettant de résoudre un problème ou d’apporter une amélioration évoquée dans une note. Une solution peut être proposée sous forme de commentaire.

Caractéristiques :

* Peut être validée par l’auteur de la note ou d’autres contributeurs en tant que réponse définitive​.  
* Peut être enrichie par des échanges dans les commentaires, menant à une solution plus aboutie​.  
* Peut inclure du code, des fichiers, des liens vers des ressources, ou toute information pertinente​.

Processus d’une Solution :

* Un utilisateur crée une note décrivant un problème ou une question​.  
* D’autres contributeurs proposent des solutions en répondant sous forme de commentaire​.  
* Une solution est validée et peut être mise en avant pour référence future​.  
* La note peut être mise à jour pour intégrer la solution directement dans son contenu​.

---

### **Contributeur**

#### Définition : Utilisateur de l’application qui participe activement en ajoutant du contenu sous forme de notes, commentaires ou tâches.

---

### **Compétence**

Définition : Aptitude technique ou fonctionnelle possédée par un contributeur et utilisée dans le cadre des projets et des tâches de l’équipe.

Caractéristiques :

* Chaque contributeur peut détailler ses compétences dans son profil utilisateur​.  
* Permet d’optimiser l’attribution des tâches et des projets​.  
* Peut être associée à des technologies spécifiques (ex. : React, PHP, MySQL) ou à des compétences transversales (ex. : gestion de projet, UX/UI)​.

Utilisation dans l’Application :

* Affichage des compétences dans le profil des contributeurs​.  
* Utilisation des compétences pour mieux répartir les développeurs sur les projets et les tâches​.  
* Permet à la direction et aux RH d’analyser les profils pour des évolutions de carrière​.

---

### **Administrateur**

Définition : Utilisateur disposant de droits avancés dans l’application pour gérer les accès, la configuration et la supervision des activités. L’administrateur peut être un membre de la direction, un chef de projet ou un responsable technique.

Rôles et Responsabilités :

* Gestion des utilisateurs : création, suppression et modification des profils​.  
* Attribution des rôles et permissions (Développeur, Chef de Projet, RH, etc.)​.  
* Supervision des rapports de participation pour suivre l’activité des équipes​.  
* Configuration des étiquettes et catégories pour l’organisation des notes​.  
* Gestion de la sécurité et de la confidentialité des données​.

Accès Spécifiques :

* Peut générer et consulter les rapports de participation pour analyser l’engagement des contributeurs​.  
* Peut modifier les paramètres globaux de l’application (ex. : règles de confidentialité, droits d’accès)​.

---

### **Rapport de Participation**

Définition : Document ou tableau de bord généré par l’application permettant de mesurer l'implication des utilisateurs dans le système de gestion de notes et de tâches. Il est utilisé pour suivre et analyser l’activité des contributeurs sur une période donnée.

Informations Contenues dans un Rapport de Participation :

* Nombre de notes créées par un utilisateur​.  
* Nombre de commentaires laissés sur les discussions​.  
* Tâches attribuées et complétées​.  
* Projets actifs et niveau d’implication de l’utilisateur​.  
* Indicateurs de collaboration : interactions avec d’autres membres de l’équipe, réponses aux discussions, suivi des feedbacks​.

Accès et Confidentialité :

* Accessible par les chefs de projet, les RH et la direction pour une analyse globale des performances​.  
* Peut être consulté par les utilisateurs pour voir leur propre niveau de contribution, mais pas celui des autres membres sans autorisation​.

---

### **Profil**

#### Définition : Ensemble d’informations associées à un utilisateur dans l’application. Le profil permet d’identifier un utilisateur, de visualiser ses compétences et de faciliter la collaboration au sein des projets.

#### Informations Contenues dans un Profil :

* #### Identité : Nom, prénom, rôle dans l’entreprise (Développeur, Chef de Projet, Alternant, etc.)​.

* #### Compétences : Technologies maîtrisées, spécialités techniques, expériences​.

* #### Projets en cours : Liste des projets auxquels l’utilisateur participe​.

* #### Historique des contributions : Notes créées, discussions commentées, tâches effectuées​.

#### Rôle du Profil dans l’Application :

* #### Permet d’assigner des tâches et des projets en fonction des compétences​.

* #### Aide les développeurs à trouver les bonnes personnes pour résoudre un problème​.

* #### Sert aux RH et à la direction pour suivre l’évolution et la participation des utilisateurs​.

#### Accès et Confidentialité :

* #### Certains éléments du profil peuvent être visibles par toute l’équipe (compétences, projets en cours).

* #### D’autres sont réservés aux administrateurs et RH (statistiques de participation, informations personnelles)​.

---

**Niveau d’accès**

Définition : Ensemble des permissions et restrictions définissant ce qu’un utilisateur peut voir, modifier ou gérer dans l’application. Le niveau d’accès dépend du rôle attribué à chaque utilisateur.

Caractéristiques :

* Contrôle l’accès aux fonctionnalités, aux notes, aux tâches et aux rapports.  
* Définit si un utilisateur peut créer, modifier ou supprimer certains éléments.  
* Peut être modifié par un administrateur en fonction des besoins organisationnels.

---

### **Rôle**

Définition : Statut attribué à un utilisateur dans l’application, déterminant ses droits et permissions. Un rôle définit ce qu’un utilisateur peut voir, modifier ou gérer au sein du système.

Principaux Rôles dans l’Application :

* Développeur : Peut créer des notes, commenter et interagir avec les tâches et discussions​.  
* Chef de Projet : Dispose des droits des développeurs. Supervise les tâches, attribue des responsabilités, consulte les métriques de participation​.  
* Administrateur : Gère les accès, modifie la configuration et supervise la plateforme​.  
* Responsable RH : Accède aux profils des contributeurs et aux rapports de participation pour le suivi des compétences​.  
* Direction : Dispose d’une vue d’ensemble sur l’activité et la productivité des équipes​.

---

### **Utilisateur**

Définition : Toute personne ayant accès à l’application et interagissant avec ses fonctionnalités. Un utilisateur peut être un contributeur actif (créant et commentant des notes) ou un observateur (consultant les informations sans y contribuer directement).

---

### **Métriques de Participation**

Définition : Indicateurs permettant de mesurer l’implication des membres de l’équipe.

Exemples :

* Nombre de notes créées  
* Nombre de commentaires laissés  
* Nombre de tâches terminées

---

#### 

### **Notification**

Définition : Une alerte envoyée à un utilisateur pour l’informer d’un nouvel élément pertinent (ex. : une nouvelle note, un commentaire sur une note suivie, une tâche assignée).

---

.

### **Répertoire**

#### Définition : Espace centralisé dans l’application permettant de consulter la liste des utilisateurs et d’accéder aux profils des contributeurs. Le répertoire facilite l’identification des collaborateurs, leur rôle et leurs compétences.

#### Caractéristiques :

* #### Contient la liste des membres de l’application (Développeurs, Chef de Projet, RH, etc.).

* #### Permet la recherche et le filtrage des utilisateurs en fonction de leur rôle, projet ou compétence.

* #### Accès contrôlé selon les niveaux d’accès, destiné aux RH.

