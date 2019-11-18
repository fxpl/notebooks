#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -t 4:00:00
#SBATCH -p core -n 1
#SBATCH -J count_clones
#SBATCH -M snowy

clonesFile="clones2019-11-10T18:21:42.078.csv"
snippetsFile="snippets2019-11-08T16:29:42.901.csv"


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


# TODO: Det här kommer inte att terminera inom rimlig tid! Skapa fractionClones i Javaprogrammet istället!?
# Create a file (clonesInFiles.csv) where each line contains the name of a
# notebook file, followed by a list of its snippets, marked by "K" if it is a
# clone and marked by "U" if it is unique.
cp $snippetsFile clonesInFiles.csv
cat numCommas.txt | while read line
do
    hash=`echo $line | cut -d' ' -f1`
    num=`echo $line | cut -d' ' -f2`
    if [ $num -gt 2 ] 
    then
        type="K"
    else
        type="U"
    fi  
    sed -Ei "s/$hash/$type/" clonesInFiles.csv
done

# Create a file (fractionClones.csv) with 1 line per notebook and 4 columns:
# file name, #cloned snippets in file, #unique snippets in file, fraction clones in file
echo "file, clones, unique, fraction clones" > fractionClones.csv
sed -n "2,$ p" clonesInFiles.csv | while read line
do
    numK=0
    numU=0
    tokens=($line)
    file=${tokens[0]}
    for type in ${tokens[@]:1}
    do  
        if [ "K," == $type ]
        then
            numK=$numK+1
        elif [ "U," == $type ]
            numU=$numU+1
        else
            echo "Unknown type $type on line $line!"
        fi
        fraction=`echo $numK/($numK+$numU) | bc`
        echo "$file, $numK, $numU, $fraction" >> fractionClones.csv
    done
done


# OTESTAT
# Check how many files contains only clones and only unique snippets respectively
## If fraction == 1, all snippets in the file are clones. (fraction<=1).
fractionClones=`sed -n "2,$ p" fractionClones.csv | cut -d' ' -f4`
onlyClones=`echo "$fractionClones" | grep "^1\." | wc -l`
## If no number in fraction > 0, fraction==0, that is all snippets in the file are unique.
onlyUnique=`echo "$fractionClones" | grep -v "[1-9]" | wc -l`
onlyClonesFrac=`echo "$onlyClones / ($onlyClones+$onlyUnique)" | bc -l`
onlyUniqueFrac=`echo "$onlyUnique / ($onlyClones+$onlyUnique)" | bc -l`
echo "Files only containing clones: $onlyClones ($onlyClonesFrac)"
echo "Files only containing unique snippets: $onlyUnique ($onlyUniqueFrac)"

numLines=`wc -l $snippetsFile | cut -d' ' -f1`
numFiles=`echo "$numLines - 1" | bc`
echo "On average the following number of snippets in a file are clones:"
echo "`echo "$fractionClones" | paste -sd+` / $numFiles" | bc -l

