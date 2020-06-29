#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 15:00
#SBATCH -J move_duplicates
#SBATCH -M snowy

################################################################################
# Find all notebooks that are stored more than once (with the same file name).
# For each of these, move all occurrences but one to a separate directory.
################################################################################

projDir="/proj/uppstore2019098/notebooks"
notebookPathsFile="$projDir/notebook_paths.txt"
duplicates="./duplicates.txt"
targetDir="/proj/uppstore2019098/duplicated_notebooks"

rev $notebookPathsFile | cut -d '/' -f1 | sort | uniq -c | sed -E "s/^\s*//" | grep -E -v "^1" | cut -d' ' -f2 | rev | while read notebook
do
	num=0
	grep $notebook $notebookPathsFile | while read path
	do
		if [ $num -ne 0 ]
		then
			notebook=`echo $path | rev | cut -d'/' -f1 | rev`
			subdir=`echo $path | rev | cut -d'/' -f2- | rev`
			mkdir -p $targetDir/$subdir
			mv $projDir/$subdir/$notebook $targetDir/$subdir
		fi
		((num++))
	done
done
