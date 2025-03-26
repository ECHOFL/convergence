
Projet Convergence
==================

Description du projet :
------------------------
Convergence est un plugin développé pour servir de plateforme modulaire sur un serveur Minecraft. 
Le projet se structure autour de différents modules, chacun gérant des fonctionnalités spécifiques.
Par exemple, le module "Prison" gère des aspects tels que :
 • La configuration des mines.
 • La gestion des enchantements liés aux outils (pioche).
 • La gestion des rangs et du prestige des joueurs.

Architecture et modules :
--------------------------
1. Core
   - Le coeur du plugin contient des classes communes utilisées dans tous les modules.
   - Par exemple, le DatabaseConnector centralise la gestion de la connexion à la base de données
     (via HikariCP) en lisant la configuration globale du plugin (config.yml, section "database").
   - Le ConfigManager gère le chargement et la sauvegarde des fichiers de configuration.

2. Module Prison
   - Le module Prison est un exemple de module qui gère des fonctionnalités spécifiques :
     - Configuration des mines et des enchantements.
     - Gestion des niveaux d’enchantements des joueurs via la table SQL dédiée.
   - La configuration propre à ce module se trouve dans "modules/prison/config.yml".
   - Les niveaux d’enchantements des joueurs sont gérés par la classe PlayerEnchantDataService,
     qui s’appuie sur la connexion centralisée fournie par le core (DatabaseConnector).

3. Autres modules possibles
   - D’autres modules (ex. gestion de ranks, de prestige, de mines personnelles, etc.) peuvent être ajoutés.
   - Chaque module dispose de sa propre configuration et d’un système de connexion dédié pour gérer
     les accès à la base de données spécifiques à leurs fonctionnalités.

Utilisation et configuration :
-------------------------------
- Le fichier de configuration principal (config.yml) contient la section "database" avec les paramètres
  de connexion à la base de données (jdbc-url, username, password).
- Les modules comme Prison possèdent leur propre configuration spécifique (exemple dans modules/prison/config.yml).
- Les enchantements sont définis dans un fichier YAML (enchants.yml) et leurs niveaux pour chaque joueur
  sont stockés dans une table SQL dont le schéma s'adapte à la liste des enchantements.
- Lors de la connexion d'un joueur, le système vérifie l'existence d'une entrée dans la table des niveaux
  d'enchantements et initialise les valeurs à 0 si nécessaire.

Démarrage du plugin :
---------------------
Le plugin se base sur une architecture modulaire. Lors du démarrage de Convergence :
 • Le ConfigManager charge la configuration principale et celle des modules.
 • Le DatabaseConnector s’instancie en récupérant les paramètres dans le fichier principal.
 • Chaque module, comme Prison, initialise ses services (managers, connecteurs spécifiques,
   gestionnaires d’enchantements, etc.) et enregistre ses commandes et listeners.
 • Des logs informatifs permettent de vérifier que chaque module a bien été activé.

Contributeurs et documentation :
----------------------------------
Pour toute question, modification ou ajout de fonctionnalités, veuillez vous référer à la documentation
interne du projet et aux conventions de développement décrites dans les fichiers sources. 

Bonne utilisation et bon développement !

L’équipe Convergence
