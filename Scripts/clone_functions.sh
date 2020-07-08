#!/bin/bash

################################################################################
# Count the number of clone groups
# Argument: Name of hash to file mapping file
################################################################################
analyzeCloneGroups() {
	hash2files=$1

	# Create file with LOC and number of occurences for each clone group
	numFilesFile="../Output/filesPerSnippet`date -Ins`.csv"
	numFiles=`sed -n "2,$ p" $hash2files | grep -o ',' -n | uniq -c | sed -E "s/^\s*//" | cut -d' ' -f1 | sed -E "s/^([0-9]+)$/(\1-1)\/2/" | bc`
	hashesAndLOC=`sed -n "2,$ p" $hash2files | cut -d',' -f1-2`
	echo "hash, LOC, count" > $numFilesFile
	paste <(echo "$hashesAndLOC") <(echo "$numFiles") >> $numFilesFile
	sed -Ei "s/\t/, /g" $numFilesFile

	# Count clone groups
	cloneGroups=`sed -n "2,$ p" $numFilesFile | cut -d' ' -f3 | grep -v "^1$" | wc -l`
	unique=`sed -n "2,$ p" $numFilesFile | cut -d' ' -f3 | grep "^1$" | wc -l`
	fractionCloneGroups=`echo "$cloneGroups / ($cloneGroups+$unique)" | bc -l`
	echo "Total number of clone groups: $cloneGroups"
	echo "Fraction clones (each clone group counted once): $fractionCloneGroups"

	# Number of snippets occurring in > 1 file
	numSnippetsInSeveralFiles=0
	while read line
	do
		tokens=($line)
		if [ ${#tokens[@]} -ge 6 ] && [ ${tokens[3]} != ${tokens[-2]} ]
		then
			numSnippetsInSeveralFiles=$(($numSnippetsInSeveralFiles + 1))
		fi
	done < $hash2files
	numLines=`wc -l $hash2files | cut -d' ' -f1`
	numSnippets=`echo "$numLines - 1" | bc`
	severalFraction=`echo "$numSnippetsInSeveralFiles / $numSnippets" | bc -l`
	severalPercent=`echo "$severalFraction * 100" | bc`
	echo "Number of snippets occurring in more than one file: \
	$numSnippetsInSeveralFiles ($severalPercent %)."
}

################################################################################
# Count the number of clones and unique snippets. Check how many files contain
# only clones and only unique snippets respectively.
# Arguments:
# 1. Index of the column in the clone frequency file that contains the
#	frequencies (different depending on if empty snippets are included.
# 2. Flag indicating whether empty snippets should be subtracted from the unique
#	 clones ("true") or not ("false"). (TODO: Går det att lösa snyggare?!)
################################################################################
analyzeClones() {
	frequencyFile=`./get_last_output.sh "cloneFrequency"`
	frequencyCol=$1
	subtract_empty_from_unique=$2

	# Total number of clones and unique snippets respectively
	numClones=`sed -n "2,$ p" $frequencyFile | cut -d',' -f3 | paste -sd+ | bc`
	numUnique=`sed -n "2,$ p" $frequencyFile | cut -d',' -f2 | paste -sd+ | bc`
	numEmpty=`sed -n "2,$ p" $frequencyFile | cut -d',' -f4 | paste -sd+ | bc`	# NOTE: Nonempty clones in old format!
	if [ "$subtract_empty_from_unique" = true ] ; then
		numUnique=`echo "$numUnique - $numEmpty" | bc`
	fi
	fractionClones=`echo "$numClones / ($numClones+$numUnique)" | bc -l`
	echo "Total number of clones (including all copies): $numClones"
	echo "Total number of empty snippets: $numEmpty"
	echo "Total number of unique snippets: $numUnique"
	echo "Fraction clone copies: $fractionClones"

	# File statistics
	## If fraction == 1, all snippets in the file are clones. (fraction<=1).
	cloneFrequencies=`sed -n "2,$ p" $frequencyFile | cut -d',' -f$frequencyCol`
	onlyClones=`echo "$cloneFrequencies" | grep " 1.0000" | wc -l`
	## If no number in fraction > 0, fraction==0, that is all snippets in the file are unique.
	onlyUnique=`echo "$cloneFrequencies" | grep " 0.0000" | wc -l`
	noSnippets=`echo "$cloneFrequencies" | grep " 0$" | wc -l` # 0 without decimals = no snippets in file
	numLines=`wc -l $frequencyFile | cut -d' ' -f1`
	numFiles=`echo "$numLines - 1" | bc`
	onlyClonesFrac=`echo "$onlyClones / $numFiles" | bc -l`
	onlyUniqueFrac=`echo "$onlyUnique / $numFiles" | bc -l`
	noSnippetsFrac=`echo "$noSnippets / $numFiles" | bc -l`
	echo "Files only containing clones: $onlyClones ($onlyClonesFrac)"
	echo "Files only containing unique snippets: $onlyUnique ($onlyUniqueFrac)"
	echo "Files containing no snippets: $noSnippets ($noSnippetsFrac)"
	echo "On average the following fraction of snippets in a file are clones:"
	echo "(`echo "$cloneFrequencies" | paste -sd+`) / $numFiles" | bc -l
}

################################################################################
# Count the number of files containing intra notebook clones
# Argument: Index of the column in the clone frequency file that contains the
#			number of intra clones.
################################################################################
intraClones() {
	frequencyFile=`./get_last_output.sh "cloneFrequency"`
	intraCloneCol=$1

	intraClones=`sed -n "2,$ p" $frequencyFile | cut -d',' -f$intraCloneCol | grep -E -v "\ 0$" | wc -l`
	noIntraClones=`sed -n "2,$ p" $frequencyFile | cut -d',' -f$intraCloneCol | grep -E "\ 0$" | wc -l`	# Sanity check

	echo "Number of files containing intra clones: $intraClones"
	echo "Number of files not containing intra clones: $noIntraClones"
}

