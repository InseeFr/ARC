# Interagir avec le système de fichiers de ARC

Ce document est une aide pour la manipulation de fichiers dans les répertoires (Applishare ou S3) de ARC.

## L'arborescence de fichiers ARC

ARC manipule la même arborescence de répertoires peu importe l'espace de stockage (S3, Applishare, etc.) : chaque bac à sable dispose d'un répertoire (**ARC_BAS1** par exemple) dans lequel peuvent se trouver plusieurs dossiers aux usages spécifiques :
- **RECEPTION_[DEPOT]** pour les fichiers déposés, qui seront lus lors de l'étape de réception
- **RECEPTION_OK** pour les fichiers bien réceptionnés
- **RECEPTION_ENCOURS** pour les fichiers en cours de réception
- **RECEPTION_KO** pour les fichiers en échec lors de la réception
- **RECEPTION_[DEPOT]_ARCHIVE** pour les fichiers archivés
- **RECEPTION_[DEPOT]_ARCHIVE_OLD** pour les fichiers archivés et récupérés par les clients ; ce répertoire est soumis à une date de rétention des fichiers
- **DOWNLOAD** pour les fichiers ou enveloppes téléchargées
- **EXPORT** pour les fichiers exportés en sortie

## Manipuler une arborescence classique : FileUtilsArc

FileUtilsArc est une classe utilitaire permettant de supprimer, renommer ou déplacer des fichiers, et créer ou supprimer des répertoires.
- **isCompletelyWritten(file)** vérifie si un fichier n'est pas en cours d'écriture
- **createDirIfNotExist(file ou path)** crée un répertoire à l'emplacement (File ou String) donné
- **deleteDirectory(file)** supprime un répertoire et tout son contenu
- **deleteAndRecreateDirectory(file)** supprime un répertoire et le recrée à vide
- **renameTo(file, newFile)** renomme un fichier
- **delete(file)** supprime un fichier
- **deplacerFichier(dirIn, dirOut, nameIn, nameOut)** déplace un fichier d'un répertoire à un autre, en le renommant éventuellement, et en écrasant la destination si elle existe déjà

## Manipuler des fichiers et répertoires dans S3 : ArcS3, S3Template

ARC dispose de classes spécifiques pour gérer les fichiers dans un système S3.

La classe ArcS3 de arc-core permet de manipuler les buckets S3 d'entrée (`INPUT_BUCKET`) et de sortie (`OUTPUT_BUCKET`) de ARC, selon les propriétés de l'application qui sont renseignées. Ces objets bucket sont des instances de la classe utilitaire S3 de ARC, S3Template.

La classe S3Template de arc-utils permet de lancer tout un ensemble d'opérations sur le système de fichiers S3 :
- **isS3Off()** vérifie si le S3 est désactivé
- **getMinioClient()** renvoie le client minio, en le recréant si nécessaire
- **createDirectory(cheminS3)** crée un nouveau répertoire au chemin indiqué. En réalité, le concept de répertoire n'existe pas dans S3, mais ARC en a besoin. Pour matérialiser un répertoire, cette méthode crée donc un fichier vide .exists au chemin indiqué. Cette méthode crée également les répertoires parents.
- **copy(cheminS3Orig, cheminS3Dest)** copie un fichier d'un emplacement à un autre
- **download(cheminS3Orig, cheminLocalDest)** télécharge un fichier du S3 vers un emplacement hors S3
- **downloadToDirectory(cheminS3Orig, cheminLocalDest)** télécharge un fichier du S3 vers un répertoire hors S3 avec le même nom d'origine
- **upload(cheminLocalOrig, cheminS3Dest)** transfère un fichier d'un répertoire hors S3 vers un emplacement S3
- **delete(cheminS3)** supprime le fichier au chemin indiqué. Cette méthode peut aussi prendre en entrée une liste de chemins.
- **deleteDirectory(cheminS3)** supprime le répertoire au chemin indiqué et son contenu
- **move(cheminS3Orig, cheminS3Dest)** déplace un fichier d'un emplacement à un autre
- **size(cheminS3)** renvoie la taille de l'objet au chemin indiqué (fichier ou répertoire), en octets
- **isExists(cheminS3)** vérifie si un objet existe au chemin donné
- **isDirectory(cheminS3)** vérifie si un chemin donné est celui d'un répertoire existant
- **listObjectsInDirectory(cheminS3, ...)** liste les objets contenus dans un répertoire. Cette méthode peut prendre les paramètres suivants (si aucun renseignés, tous à false) :
    - **isRecursive** : si true, liste également les objets contenus dans les sous-répertoires
    - **includeExists** : si true, liste également les fichiers .exists (indicateurs d'existence d'un répertoire)
    - **includeSubdirs** : si true, liste également les noms des sous-répertoires
- **closeMinioClient()** ferme la connexion à minio

