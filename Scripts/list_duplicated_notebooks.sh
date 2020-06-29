#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -t 30:00
#SBATCH -p core -n 1
#SBATCH -M snowy

################################################################################
# Let a cell sequence be all code cells in a notebook, in order. List each
# unique cell sequence preceded by the number of notebooks containing exactly
# that sequence in nb_clone_specA.csv and nb_clone_specNE.csv respectively.
# Also print each count from the nb_clone_spec files to nb_clone_distrA.csv and
# nb_clone_distrNE.csv respectively.
# Last, for each cell sequence that occurs more than once, list the names of
# all notebooks containing exactly that cell sequence, one cell sequence per
# line in nb_clone_listA.csv and nb_clone_listNE.csv respectively.
# File whose name end with "NE.csv" are produced excluding empty snippets, while
# file whose name end with "A.csv" are produced using all snippets.
# All files are sorted in descending order (data for the most common notebooks
# on the first line).
################################################################################

################################################################################
# Create the files described in the comment above.
# Argument 1: Name of file containing mapping from notebook (file) to hashes
# Argument 2: Suffix of the output files
################################################################################
listDuplicated() {
	f2h=$1
	suffix=$2
	specs="../Output/nb_clone_spec$suffix.csv"
	count="../Output/nb_clone_distr$suffix.csv"
	list="../Output/nb_clone_list$suffix.csv"

	sed -n "2,$ p" $f2h | cut -d' ' -f2- | grep -E -v "\.ipynb" | sort | uniq -c | sort -rn > $specs

	sed -E "s/([0-9]) ([A-F,0-9])/\1, \2/" $specs | cut -d',' -f1 > $count

	sed -E "s/([0-9]) ([A-F,0-9])/\1, \2/" $specs | grep -E -v "^\s*1\, " | cut -d',' -f2- | while read hashComb
	do
		grep -E "ipynb\, $hashComb$" $f2h | cut -d',' -f1 | paste -sd' ' >> $list
	done
	echo "--------------------------------------------------------------------------------" >> $list

}

file2hashesA=`./get_latest_output.sh "file2hashesA"`
file2hashesNE=`./get_latest_output.sh file2hashesNE`
listDuplicated $file2hashesA "A"
listDuplicated $file2hashesNE "NE"

