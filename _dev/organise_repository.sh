#!/bin/bash
# organise_repository.sh
# Script permettant de reorganiser un repository "Situations" en repository "Cannelle" (les fichiers des rapports sont regroupés par répertoires alors qu'ils étaient en "vrac" dans Situations.
# 1. Faire une sauvegarde du repository
# 2. Placer le script dans le répertoire du repository
# 3. Lancer le script
# 4. Si tout s'est bien passé, les repertoires sont créés et un répertoire _back_organise contient le repository original.
################################### 
echo
directory=$PWD

echo $directory


for file in *
do
  echo $file
  if [ -f $file ] 
  	then
  	
  	extension=${file/*./}
  	#echo $extension
  	
  	if [ ! $extension == "sh"  ]
  		then	
		echo "Traitement de \"$file\"".
		#recuperer le nom du repertoire associe
		nomRepertoire=${file/%.*/} 
	  
		echo "$nomRepertoire"
		#echo
		#creer le repertoire s il n'existe pas
		if [ ! -d $nomRepertoire ] 
		then
			mkdir $nomRepertoire
			echo "$nomRepertoire créé"
		 else
			echo "$nomRepertoire trouvé"
		fi
		
		#copier le fichier dans le repertoire
		cp $file "$nomRepertoire/."
		
		#creer un backup
		bckupdir="$directory/_back_organise"
		if [ ! -d "$bckupdir" ] 
		then
			mkdir "$bckupdir"
			echo "$bckupdir créé"
		fi
		mv $file "${bckupdir}/."
	 fi
	echo
	echo
  fi
done
echo
exit 0