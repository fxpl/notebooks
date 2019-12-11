#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 10:00:00
#SBATCH -J move_unparseable
#SBATCH -M snowy

projDir="/proj/uppstore2019098/notebooks"
logFile="../logs/analyze-hash-complete3.out"
unparseable="./unparseable.txt"
targetDir="$projDir/unparseable_notebooks"

# Find unparseable notebooks
egrep -a "Skipping notebook\!" $logFile | cut -d'/' -f5- | cut -d':' -f1 > $unparseable

# Create parallel directory structure
rev $unparseable | cut -d'/' -f2- | rev | uniq | while read subdir;
do
	mkdir -p $targetDir/$subdir
done

# Move notebooks
while read nbPath;
do
	notebook=`echo $nbPath | rev | cut -d'/' -f1 | rev`
	subdir=`echo $nbPath | rev | cut -d'/' -f2- | rev`
	mv $projDir/$subdir/$notebook $targetDir/$subdir
done < $unparseable

