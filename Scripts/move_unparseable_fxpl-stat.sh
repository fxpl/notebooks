#!/bin/bash

################################################################################
# Find as many unparseable notebooks as possible in analyzer log file and move
# these to a separate directory.
################################################################################

diskPath="/home/maka4186/notebook_disk"
srcDir="$diskPath/notebooks"
logFile="../Logs/analyzer-all-with-unparseable.out"
unparseable="../Logs/unparseable.txt"
targetDir="$diskPath/unparseable_notebooks"

mkdir -p $targetDir

egrep "Could not get" $logFile | cut -d'_' -f2 | cut -d'.' -f1 | uniq -c | sed "s/^\s*//" > $unparseable
echo "Notebook that have errors, but are not skipped:"
cat $unparseable | grep -v "^5"

# Move notebooks
cat $unparseable | grep "^5" | cut -d' ' -f2 | while read nbNumber
do
	notebook="nb_$nbNumber.ipynb"
	mv $srcDir/$notebook $targetDir
done

