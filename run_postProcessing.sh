#!/bin/bash

################################################################################
# Run all relevant post processing scripts except the R script on the output of
# NotebookAnalyzer and SccOutputAnalyzer.
################################################################################

# Postprocessing for NotebookAnalyzer result
ln -s OutputNBA Output
cd Scripts
./language_analysis.sh > ../Output/output_language_analysis.txt
./language_inconsistencies.sh > ../Output/output_language_inconsistencies.txt
./count_nonempty_snippets.sh
./clone_analysis.sh > ../Output/output_clone_analysis.txt
./print_most_common_snippets.sh 100 0 1 > ../Output/top100clones_min0.txt
./print_most_common_snippets.sh 100 4 1 > ../Output/top100clones_min4loc.txt
./list_duplicated_notebooks.sh

# If a notebook is written in Python and exists in the file2hashes file in
# OutputNBA but not in OutputSCC, it is empty. Find these and add to
# file2hashes, cloneFrequency and connections files in OutputSCC (since empty
# notebooks are not included in SourcererCC analysis).
langFile=`./get_latest_output.sh "languages"`
pythonNotebooks="pythonNotebooks.txt"
grep -E "^nb_[0-9]+\.ipynb\, PYTHON\, " $langFile | cut -d',' -f1 > $pythonNotebooks
cd ..
rm Output
ln -s OutputSCC Output
cd Scripts
file2hashesSCC=`./get_latest_output.sh "file2hashes"`
cloneFreqSCC=`./get_latest_output.sh "cloneFrequency"`
connectionsSCC=`./get_latest_output.sh "connections"`
cd ../Scripts
sccNotebooks="sccNotebooks.txt"
sed -n "2,$ p" $file2hashesSCC | cut -d',' -f1 > $sccNotebooks
grep -vFf sccNotebooks.txt pythonNotebooks.txt | while read notebook; do
	echo "$notebook" >> $file2hashesSCC
	echo "$notebook, 0, 0, 0, 0, 0, 0, 0" >> $cloneFreqSCC
	echo "$notebook, 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000" >> $connectionsSCC
done
rm $pythonNotebooks
rm $sccNotebooks

# Postprocessing for SourcererCC result
# Note: This is not properly tested since we don't have the file2hashesNE yet
./count_nonempty_snippets.sh
./clone_analysis.sh > ../Output/output_clone_analysis.txt
./print_most_common_snippets.sh 100 0 10 > ../Output/top100clones_min0.txt
./print_most_common_snippets.sh 100 4 10 > ../Output/top100clones_min4loc.txt
./list_duplicated_notebooks.sh
cd ..
rm Output

