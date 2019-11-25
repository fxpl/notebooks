#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -t 4:00:00
#SBATCH -p core -n 1
#SBATCH -J count_clones
#SBATCH -M snowy

clonesFile="clones2019-11-10T18:21:42.078.csv"
snippetsFile="snippets2019-11-08T16:29:42.901.csv"
frequencyFile="fractionClones.csv"

# Most clones snippets
sortedCloneCount=`grep -o ',' -n $clonesFile | uniq -c | sort -n`
echo "Most cloned snippets:"
echo "$sortedCloneCount" | tail
echo ""
# Sanity check
echo "Least cloned snippets:"
echo "$sortedCloneCount" | head
echo ""

# OTESTAT
# Number of snippets occurring in > 1 file
numSnippetsInSeveralFiles=0
while read line
do
	tokens=($line)
	if [ ${#tokens[@]} -ge 5 ] && [ ${tokens[1]} != ${tokens[-2]} ]
	then
		numSnippetsInSeveralFiles=$(($numSnippetsInSeveralFiles + 1))
	fi
#done < "$(sed -n "2,$ p" $clonesFile)"	# Funkar inte...
done < $clonesFile
numLines=`wc -l $clonesFile | cut -d' ' -f1`
numSnippets=`echo "$numLines - 1" | bc`
severalFraction=`echo "$numSnippetsInSeveralFiles / $numSnippets" | bc -l`
severalPercent=`echo "$severalFraction * 100" | bc`
echo "Number of snippets occurring in more than one file: \
$numSnippetsInSeveralFiles ($severalPercent %)."


# Number of clones and unique snippets respectively
cloneCount=`sed -n "2,$ p" $clonesFile | grep -o ',' -n | uniq -c | sed -E "s/^\s*//" | cut -d' ' -f1`
hashes=`sed -n "2,$ p" $clonesFile | cut -d',' -f1`
paste <(echo "$hashes") <(echo "$cloneCount") > numCommas.txt
sed -Ei "s/\t/ /" numCommas.txt

clones=`cut numCommas.txt -d' ' -f2 | grep -v "^2$" | wc -l`
unique=`cut numCommas.txt -d' ' -f2 | grep "^2$" | wc -l`
fraction=`echo "$clones / ($clones+$unique)" | bc -l`
echo "Total number of clones: $clones"
echo "Total number of unique snippets: $unique"
echo "Fraction clones: $fraction"

exit


# OTESTAT
# Check how many files contains only clones and only unique snippets respectively
## If fraction == 1, all snippets in the file are clones. (fraction<=1).
fractionClones=`sed -n "2,$ p" $frequencyFile | cut -d' ' -f4`
onlyClones=`echo "$fractionClones" | grep "^1\." | wc -l`
## If no number in fraction > 0, fraction==0, that is all snippets in the file are unique.
onlyUnique=`echo "$fractionClones" | grep -v "[1-9]" | wc -l`
onlyClonesFrac=`echo "$onlyClones / ($onlyClones+$onlyUnique)" | bc -l`
onlyUniqueFrac=`echo "$onlyUnique / ($onlyClones+$onlyUnique)" | bc -l`
echo "Files only containing clones: $onlyClones ($onlyClonesFrac)"
echo "Files only containing unique snippets: $onlyUnique ($onlyUniqueFrac)"

numLines=`wc -l $frequencyFile | cut -d' ' -f1`
numFiles=`echo "$numLines - 1" | bc`
echo "On average the following fraction of snippets in a file are clones:"
echo "`echo "$fractionClones" | paste -sd+` / $numFiles" | bc -l

