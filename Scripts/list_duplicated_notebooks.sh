#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -t 1:00:00
#SBATCH -p core -n 1
#SBATCH -M snowy

f2h=`./get_latest_output.sh "file2hashes"`
duplicates=../Output/duplicates_sorted.txt
mostDuplicated=../Output/most_duplicated_notebooks.txt

# For each combination of hashes (snippets), find the notebooks containing
# this combination (in order). Sort the output on the number of notebooks
# (descending) and write the result to the file specified above
# ($mostDuplicated)
sed -n "2,$ p" $f2h | cut -d' ' -f2- | egrep -v "\.ipynb" | sort | uniq -c | sort -rn > $duplicates

sed -E 's/([0-9]) ([A-F,0-9])/\1, \2/' $duplicates | cut -d',' -f2- | while read hashComb
do
	egrep "ipynb\, $hashComb$" $f2h | cut -d',' -f1 | paste -sd' ' >> $mostDuplicated
done
echo "--------------------------------------------------------------------------------" >> $mostDuplicated
