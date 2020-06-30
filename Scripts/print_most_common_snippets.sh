#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 1:00:00
#SBATCH -p core -n 1
#SBATCH -J notebook_snippetPrinter

################################################################################
# Find the largest clone groups with median LOC >= the specified value. Print
# the specified number of snippets from each of these this clone group to stdout.
# The snippets to print are selected at random from the clone group.
#
# Arguments:
# 1: Number of clone groups to print
# 2: Minimum number of LOC for printed snippets
# 3: Number of snippets to print from each clone group
################################################################################
numCloneGroups=$1
minLOC=$2
snippetsPerCloneGroup=$3

hashFile=`./get_latest_output.sh "hash2filesA"`
diskPath=/home/maka4186/notebook_disk
pathFile=$diskPath/notebook_paths.txt

################################################################################
# Generate the specified number of random integers, all with unique values
# between 1 and the specified max value (inclusive). Print result to stdout.
#
# Arguments:
# 1: Maximum value of random numbers
# 2: Number of values to generate
################################################################################
function generate_random_numbers() {
	if [ $# -ne 2 ]; then
		echo "Two parameters expected: Maximum value and number of values"
		exit
	fi

	MAX_VAL=$1
	COUNT=$2
	INT_MAX=`echo "2^32" | bc`

	result=()
	while [ ${#result[@]} -lt $COUNT ]; do
		rand=`od -An -N4 -i < /dev/urandom`
		if [[ $rand -lt "0" ]]; then
			rand=`echo "$rand+$INT_MAX" | bc`
		fi
		rand=`echo "$rand*$MAX_VAL/$INT_MAX + 1" | bc`
		if [[ ! " ${result[@]} " =~ " ${rand} " ]]; then
			result+=( $rand )
		fi
	done

	echo ${result[@]}
}


i=1
# Print $snippetsPerCloneGroup random occurrences of the $numCloneGroups most
# common clones using SnippetPrinter (=> stdout at the time of writing)
sed -n "2,$ p" $hashFile | grep -o ',' -n | uniq -c | sort -rn \
 | cut -d':' -f1 | rev | cut -d' ' -f1 | rev | while read lineNum; do

 	lineNum=$(($lineNum+1))		# Since we skip header (read from line 2) above
	line=`sed -n "$lineNum, $lineNum p" $hashFile`
	snippetLength=`echo $line | cut -d',' -f2`

	if [ $snippetLength -ge $minLOC ];
	then
		# Number of occurrences of the snippet
		numOccurrences=`echo $line | grep -o ',' -n | uniq -c | rev | cut -d' ' -f2 | rev`
		numOccurrences=`echo "($numOccurrences-1)/2" | bc`

		# Print the specified number of random occurrences
		echo "$i ($numOccurrences OCCURRENCES):"
		snippetIndices=`generate_random_numbers $numOccurrences $snippetsPerCloneGroup`
		for snippetIndex in ${snippetIndices[@]};
		do
			# Information about the snippet
			notebookIndex=`echo "2 * $snippetIndex + 1" | bc`
			snippetIndexIndex=`echo "$notebookIndex + 1" | bc`
			notebook=`echo $line | cut -d',' -f${notebookIndex}`
			snippetIndex=`echo $line | cut -d',' -f${snippetIndexIndex} | tr -d ' '`
			notebookPath=$diskPath"/"`grep $notebook $pathFile`

			# Print snippet
			echo "----------------------------------------"
			echo "$notebookPath($snippetIndex) :"
			java -XX:+UseParallelGC -cp ../Programs/bin:../Programs/external/json-20190722.jar \
				notebooks.SnippetPrinter $notebookPath $snippetIndex
		done
		echo "================================================================================"

		i=$((i+1))
	fi

	if [ $i -gt $numCloneGroups ];
	then
		exit
	fi
 done

