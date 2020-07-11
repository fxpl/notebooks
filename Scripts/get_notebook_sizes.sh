#!/bin/bash

################################################################################
# Measure the size on disk of each notebook, in bytes.
################################################################################

source ../paths.sh
pathFileDir=`echo $notebookPathsFile | rev | cut -d'/' -f2- | rev`
sizeFile="../Output/notebook_sizes.csv"

# Create csv file containing the size of each non-forked parseable notebook (in bytes)
echo "notebook, bytes" > $sizeFile
sed -n "2,$ p" $notebookPathsFile | while read nbPath
do
	size=`du --bytes $pathFileDir/$nbPath | cut -d'/' -f1 | sed -E "s/([0-9]*)\s/\1/"`
	notebook=`echo $nbPath | rev | cut -d'/' -f1 | rev`
	echo "$notebook, $size" >> $sizeFile
done

