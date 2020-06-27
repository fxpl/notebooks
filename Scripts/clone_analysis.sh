#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -t 4:00:00
#SBATCH -p core -n 1
#SBATCH -J count_clones
#SBATCH -M snowy


### Functions ###

################################################################################
# Count the number of clones and unique snippets.
# Argument: Name of hash to file mapping file
################################################################################
countClones() {
	hash2files=$1
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

	# Number of clones and unique snippets respectively
	numFilesFile="../Output/filesPerSnippet`date -Ins`.csv"
	numFiles=`sed -n "2,$ p" $hash2files | grep -o ',' -n | uniq -c | sed -E "s/^\s*//" | cut -d' ' -f1 | sed -E "s/^([0-9]+)$/(\1-1)\/2/" | bc`
	hashesAndLOC=`sed -n "2,$ p" $hash2files | cut -d',' -f1-2`
	echo "hash, LOC, count" > $numFilesFile
	paste <(echo "$hashesAndLOC") <(echo "$numFiles") >> $numFilesFile
	sed -Ei "s/\t/, /g" $numFilesFile

	cloneGroups=`sed -n "2,$ p" $numFilesFile | cut -d' ' -f3 | grep -v "^1$" | wc -l`
	cloneCopies=`sed -n "2,$ p" $numFilesFile | cut -d' ' -f3 | grep -v "^1$" | paste -sd+ | bc`
	unique=`sed -n "2,$ p" $numFilesFile | cut -d' ' -f3 | grep "^1$" | wc -l`
	fractionCloneGroups=`echo "$cloneGroups / ($cloneGroups+$unique)" | bc -l`
	fractionCloneCopies=`echo "$cloneCopies / ($cloneCopies+$unique)" | bc -l`
	echo "Total number of clone pairs/groups: $cloneGroups"
	echo "Total number of clones (including all copies): $cloneCopies"
	echo "Total number of unique snippets: $unique"
	echo "Fraction clones (each clone group counted once): $fractionCloneGroups"
	echo "Fraction clone copies: $fractionCloneCopies"
}

################################################################################
# Check how many files contain only clones and only unique snippets respectively.
# Argument: Index of the column in the clone frequency file that contains the
#			frequencies.
################################################################################
cloneFrequency() {
	frequencyFile=`./get_latest_output.sh "cloneFrequency"`
	frequencyCol=$1

	## If fraction == 1, all snippets in the file are clones. (fraction<=1).
	fractionClones=`sed -n "2,$ p" $frequencyFile | cut -d',' -f$frequencyCol`
	onlyClones=`echo "$fractionClones" | grep " 1.0000" | wc -l`
	## If no number in fraction > 0, fraction==0, that is all snippets in the file are unique.
	onlyUnique=`echo "$fractionClones" | grep " 0.0000" | wc -l`
	noSnippets=`echo "$fractionClones" | grep " 0$" | wc -l` # 0 without decimals = no snippets in file

	numLines=`wc -l $frequencyFile | cut -d' ' -f1`
	numFiles=`echo "$numLines - 1" | bc`
	onlyClonesFrac=`echo "$onlyClones / $numFiles" | bc -l`
	onlyUniqueFrac=`echo "$onlyUnique / $numFiles" | bc -l`
	noSnippetsFrac=`echo "$noSnippets / $numFiles" | bc -l`
	echo "Files only containing clones: $onlyClones ($onlyClonesFrac)"
	echo "Files only containing unique snippets: $onlyUnique ($onlyUniqueFrac)"
	echo "Files containing no snippets: $noSnippets ($noSnippetsFrac)"

	echo "On average the following fraction of snippets in a file are clones:"
	echo "(`echo "$fractionClones" | paste -sd+`) / $numFiles" | bc -l
}

################################################################################
# Count the number of files containing intra clones
# Argument: Index of the column in the clone frequency file that contains the
#			number of intra clones.
################################################################################
intraClones() {
	frequencyFile=`./get_latest_output.sh "cloneFrequency"`
	intraCloneCol=$1

	intraClones=`sed -n "2,$ p" $frequencyFile | cut -d',' -f$intraCloneCol | egrep -v "\ 0$" | wc -l`
	noIntraClones=`sed -n "2,$ p" $frequencyFile | cut -d',' -f$intraCloneCol | egrep "\ 0$" | wc -l`	# Sanity check

	echo "Number of files containing intra clones: $intraClones"
	echo "Number of files not containing intra clones: $noIntraClones"
}




# Create hash2files file without empty snippets
hash2filesA=`./get_latest_output.sh "hash2filesA"`
hash2filesNE=`echo $hash2filesA | sed -E "s/hash2filesA/hash2filesNE/"`
emptyPattern="^[A-F,a-f,0-9]+\, 0\, nb_"	# Pattern for line representing an empty snippet
sed -E "/$emptyPattern/d" $hash2filesA > $hash2filesNE

# Perform analyses
echo ""
echo "NUMBER OF CLONES, ALL SNIPPETS:"
countClones $hash2filesA
intraClones 7
echo ""
echo "CLONE FREQUENCY DATA, ALL SNIPPETS:"
cloneFrequency 5
echo ""
echo ""
echo "NUMBER OF CLONES, EMPTY SNIPPETS EXCLUDED:"
countClones $hash2filesNE
intraClones 8
echo ""
echo "CLONE FREQUENCY DATA, EMPTY SNIPPETS EXCLUDED:"
cloneFrequency 6
echo ""
echo ""

