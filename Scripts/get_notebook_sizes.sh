#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 250:00:00
#SBATCH -J extend_cloneFreq
#SBATCH -M snowy

################################################################################
# Measure the size on disk of each notebook, in bytes.
################################################################################

sizeFile="../Output/notebook_sizes.csv"
diskPath="/home/maka4186/notebook_disk"
pathFile="$diskPath/notebook_paths.txt"

# Create csv file containing the size of each non-forked parseable notebook (in bytes)
echo "notebook, bytes" > $sizeFile
sed -n "2,$ p" $pathFile | while read nbPath
do
	size=`du --bytes $diskPath/$nbPath | cut -d'/' -f1 | sed -E "s/([0-9]*)\s/\1/"`
	notebook=`echo $nbPath | rev | cut -d'/' -f1 | rev`
	echo "$notebook, $size" >> $sizeFile
done

