#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 250:00:00
#SBATCH -J extend_cloneFreq
#SBATCH -M snowy

################################################################################
# Add info about sizes and languages to clone frequency file.
################################################################################

sizeFile="../output/notebook_sizes.csv"

# Create csv file containing the size of each non-forked parseable notebook (in bytes)
nbDir="/proj/uppstore2019098/notebooks"
pathFile="$nbDir/notebook_paths.txt"
echo "notebook, bytes" > $sizeFile
sed -n "2,$ p" $pathFile | cut -d'/' -f2- | while read nbPath
do
	path="$nbDir/$nbPath"
	size=`du --bytes $nbDir/$nbPath | cut -d'/' -f1 | sed -E "s/([0-9]*)\s/\1/"`
	notebook=`echo $path | rev | cut -d'/' -f1 | rev`
	echo "$notebook, $size" >> $sizeFile
done

# Create csv file containing both sizes, language and clone frequencies
locFile=`./get_latest_output.sh "loc"`
langFile=`./get_latest_output.sh "languages"`
cloneFreq=`./get_latest_output.sh "cloneFrequency"`
cloneFreqWithSizes="../output/extendedCloneFrequency.csv"
header1=`head -1 $cloneFreq | cut -d',' -f1`
header2=`head -1 $locFile | cut -d',' -f2-3`
header3=`head -1 $sizeFile | cut -d',' -f2`
header4=`head -1 $langFile | cut -d',' -f2`
header5=`head -1 $cloneFreq | cut -d',' -f2-4`
echo "$header1, $header2, $header3", $header4, $header5 > $cloneFreqWithSizes
sed -n "2,$ p" $cloneFreq | while read line;
do
	notebook=`echo $line | cut -d',' -f1`
	frequencies=`echo $line | cut -d',' -f2-4`
	loc=`grep -F $notebook $locFile | cut -d',' -f2-3`
	size=`grep -F $notebook $sizeFile | cut -d',' -f2`
	lang=`grep -F $notebook $langFile | cut -d',' -f2`
	echo "$notebook, $loc, $size, $lang, $frequencies" >> $cloneFreqWithSizes
done
