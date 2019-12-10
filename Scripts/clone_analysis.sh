#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -t 4:00:00
#SBATCH -p core -n 1
#SBATCH -J count_clones
#SBATCH -M snowy

hash2files=`./get_latest_output.sh "hash2files"`
frequencyFile=`./get_latest_output.sh "cloneFrequency"`

# Most clones snippets
sortedCloneCount=`grep -o ',' -n $hash2files | uniq -c | sort -n`
echo "Most cloned snippets:"
echo "$sortedCloneCount" | tail
echo ""
# Sanity check
echo "Least cloned snippets:"
echo "$sortedCloneCount" | head
echo ""

# Number of snippets occurring in > 1 file
numSnippetsInSeveralFiles=0
while read line
do
	tokens=($line)
	if [ ${#tokens[@]} -ge 5 ] && [ ${tokens[1]} != ${tokens[-2]} ]
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
numCommas="../output/numCommas.txt"
cloneCount=`sed -n "2,$ p" $hash2files | grep -o ',' -n | uniq -c | sed -E "s/^\s*//" | cut -d' ' -f1`
hashes=`sed -n "2,$ p" $hash2files | cut -d',' -f1`
paste <(echo "$hashes") <(echo "$cloneCount") > $numCommas
sed -Ei "s/\t/ /" $numCommas

clones=`cut $numCommas -d' ' -f2 | grep -v "^2$" | wc -l`
unique=`cut $numCommas -d' ' -f2 | grep "^2$" | wc -l`
fraction=`echo "$clones / ($clones+$unique)" | bc -l`
echo "Total number of clones: $clones"
echo "Total number of unique snippets: $unique"
echo "Fraction clones: $fraction"

# Check how many files contain only clones and only unique snippets respectively
## If fraction == 1, all snippets in the file are clones. (fraction<=1).
fractionClones=`sed -n "2,$ p" $frequencyFile | cut -d' ' -f4`
onlyClones=`echo "$fractionClones" | grep "1.0000" | wc -l`
## If no number in fraction > 0, fraction==0, that is all snippets in the file are unique.
onlyUnique=`echo "$fractionClones" | grep "0.0000" | wc -l`		# 0 without decimals = no snippets in file
noSnippets=`echo "$fractionClones" | grep "^0$" | wc -l`		# 0 without decimals = no snippets in file

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

