#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 250:00:00
#SBATCH -J extend_cloneFreq
#SBATCH -M snowy

################################################################################
# Add info about sizes and languages to clone frequency file.
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

# Create csv file containing both sizes, language and clone frequencies
codeCells=`./get_latest_output.sh "code_cells"`
locFile=`./get_latest_output.sh "loc"`
langFile=`./get_latest_output.sh "languages"`
cloneFreq=`./get_latest_output.sh "cloneFrequency"`
cloneFreqWithSizes="../Output/extendedCloneFrequency.csv"
header1=`head -1 $cloneFreq | cut -d',' -f1`
header2=`head -1 $codeCells | cut -d',' -f2`
header3=`head -1 $locFile | cut -d',' -f2-3`
header4=`head -1 $sizeFile | cut -d',' -f2`
header5=`head -1 $langFile | cut -d',' -f2`
header6=`head -1 $cloneFreq | cut -d',' -f2-4`
echo "$header1, $header2, $header3", $header4, $header5, $header6 > $cloneFreqWithSizes
sed -n "2,$ p" $cloneFreq | while read line;
do
	notebook=`echo $line | cut -d',' -f1`
	frequencies=`echo $line | cut -d',' -f2-4`
	cells=`grep -F $notebook $codeCells | cut -d',' -f2`
	loc=`grep -F $notebook $locFile | cut -d',' -f2-3`
	size=`grep -F $notebook $sizeFile | cut -d',' -f2`
	lang=`grep -F $notebook $langFile | cut -d',' -f2`
	echo "$notebook,$cells,$loc,$size,$lang,$frequencies" >> $cloneFreqWithSizes
done
