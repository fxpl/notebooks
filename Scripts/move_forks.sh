#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 2:00:00
#SBATCH -J move_forks
#SBATCH -M snowy

################################################################################
# Move all fork notebooks, that is notebooks that come from repositories that
# are forks of other repositories, to a separate directory.
################################################################################

projDir="/proj/uppstore2019098/notebooks"
forks="$projDir/forked_notebooks.txt"
paths="$projDir/file_list.txt"
targetDir="/proj/uppstore2019098/forked_notebooks"

# Create parallel directory structure
grep -F -f $forks $paths | rev | cut -d'/' -f2- | rev | uniq | cut -d'/' -f2- | while read subDir;
do
	mkdir -p $targetDir/$subDir
done

# Move forked notebooks
grep -F -f $forks $paths | cut -d'/' -f2- | while read notebook;
do
	mv $projDir/$notebook $targetDir/$notebook
done

