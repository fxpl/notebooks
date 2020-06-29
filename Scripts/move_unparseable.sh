#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 1:00:00
#SBATCH -J move_unparseable
#SBATCH -M snowy

################################################################################
# Find as many unparseable notebooks as possible in analyzer log file and move
# these to a separate directory.
################################################################################

projDir="/proj/uppstore2019098/notebooks"
#logFile="../Logs/analyze-hash-complete3.out"
logFile="../Logs/analyze-all-complete.out"
unparseable="../Logs/unparseable.txt"
targetDir="/proj/uppstore2019098/unparseable_notebooks"

# Find unparseable notebooks
grep -E -a "Skipping notebook\! | Could not parse " $logFile | grep -a "/proj/uppstore2019098/notebooks/" | cut -d'/' -f5- | cut -d':' -f1 > $unparseable

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

