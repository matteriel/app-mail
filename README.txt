---------------------------------------------------------------------------
------------ Bienvenue dans l'appli de collecte des emails ----------------
---------------------------------------------------------------------------

Voici quelques informations avant de commencer :

## Lancer le projet

La méthode "main" se trouve dans "appmail-business", le pom.xml de ce projet contient le lien vers la classe Main (com.itesoft.mtp.business.ProjetBusiness)



---------------------------------------------------------------------------
------------- Application.properties --------------------------------------
---------------------------------------------------------------------------

contenu dans src/java/resources/ du projet appmail-business

## Paramétrer le projet
 
Dans ce fichier, vous pouvez paramétrer :
	- le pool de connexion (email, pwd, host, etc..)
	- le protocole à utiliser (sont supportés : pop, imap et ews)
	- le répertoire local dans lequel sauvegarder vos mails	

## Gérer le filtre

Vous pouvez également configurer, dans ce même fichier, les filtres :
	- l’email de l’émetteur du mail (obligatoire)
	- un mot présent dans le sujet du mail
	- la présence de pièces jointes (true pour la présence de PJ, false pour l'absence, toutes autres valeurs implique que ce filtre ne sera pas appliqué)

## API Reference

Les deux principales API utilisées sont 
	- jvax.mail (protocol POP et IMAP)
	- ews-java-api (protocol Microsoft EWS)

