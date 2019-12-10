#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 15:00
#SBATCH -p core -n 1
#SBATCH -J notebook_snippetPrinter

hashFile=`./get_latest_output.sh "hash2files"`
projPath=/proj/uppstore2019098/notebooks
pathFile=$projPath/file_list.txt
numSnippets=100

i=1
# Print the $numSnippets most common clones using SnippetPrinter (=> stdout at
# the time of writing)
sed -n "2,$ p" $hashFile | grep -o ',' -n | uniq -c | sort -rn | head -$numSnippets \
 | cut -d':' -f1 | rev | cut -d' ' -f1 | rev | while read lineNum; do

 	lineNum=$(($lineNum+1))		# Since we skip header (read from line 2) above
	line=`sed -n "$lineNum, $lineNum p" $hashFile`

	# Information about the snippet
	notebook=`echo $line | cut -d',' -f2`
	snippetIndex=`echo $line | cut -d',' -f3 | tr -d ' '`
	notebookPath=`grep $notebook $pathFile | cut -d'/' -f2-`
	notebookPath=$projPath"/"`grep $notebook $pathFile | cut -d'/' -f2-`

	# Number of occurrences of the snippet
	numOccurrences=`grep "$notebook, $snippetIndex," $hashFile | grep -o ',' -n | uniq -c | rev | cut -d' ' -f2 | rev`
	numOccurrences=`echo "$numOccurrences/2" | bc`

	# Print snippet
	echo ""
	echo "$i ($numOccurrences occurrences). $notebookPath($snippetIndex) :"
	java -XX:+UseParallelGC -cp bin:./external/json-simple-1.1.1.jar \
		notebooks.SnippetPrinter $notebookPath $snippetIndex
	echo "--------------------------------------------------------------------------------"

	i=$((i+1))
 done

