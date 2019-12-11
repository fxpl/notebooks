#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 15:00
#SBATCH -p core -n 1
#SBATCH -J notebook_snippetPrinter

hashFile=`./get_latest_output.sh "hash2files"`
projPath=/proj/uppstore2019098/notebooks
pathFile=$projPath/file_list.txt
numSnippets=100	# Number of snippets to print
minLength=6	# Minimum number of lines in snippet

i=1
# Print the $numSnippets most common clones using SnippetPrinter (=> stdout at
# the time of writing)
sed -n "2,$ p" $hashFile | grep -o ',' -n | uniq -c | sort -rn \
 | cut -d':' -f1 | rev | cut -d' ' -f1 | rev | while read lineNum; do

 	lineNum=$(($lineNum+1))		# Since we skip header (read from line 2) above
	line=`sed -n "$lineNum, $lineNum p" $hashFile`
	length=`echo $line | cut -d',' -f2`

	if [ $length -ge $minLength ];
	then
		# Information about the snippet
		notebook=`echo $line | cut -d',' -f3`
		snippetIndex=`echo $line | cut -d',' -f4 | tr -d ' '`
		notebookPath=`grep $notebook $pathFile | cut -d'/' -f2-`
		notebookPath=$projPath"/"`grep $notebook $pathFile | cut -d'/' -f2-`

		# Number of occurrences of the snippet
		numOccurrences=`egrep "$notebook, $snippetIndex," $hashFile | grep -o ',' -n | uniq -c | rev | cut -d' ' -f2 | rev`
		numOccurrences=`echo "($numOccurrences-1)/2" | bc`

		# Print snippet
		echo ""
		echo "$i ($numOccurrences occurrences). $notebookPath($snippetIndex) :"
		java -XX:+UseParallelGC -cp ../Programs/bin:../Programs/external/json-simple-1.1.1.jar \
			notebooks.SnippetPrinter $notebookPath $snippetIndex
		echo "--------------------------------------------------------------------------------"

		i=$((i+1))
	fi

	if [ $i -gt $numSnippets ];
	then
		exit
	fi
 done

