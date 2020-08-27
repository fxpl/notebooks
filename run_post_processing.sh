#!/bin/bash

################################################################################
# Run all relevant post processing scripts on the output of NotebookAnalyzer and
# SccOutputAnalyzer.
################################################################################

source paths.sh


# PREPARATIONS:

# If a notebook is written in Python and is not included in the clone frequency
# file in outputSOA, it is empty. Find these and add to cloneFrequency and
# connections files in outputSOA (since empty notebooks are not included among
# SourcererCC clones).
ln -s $outputNBA Output
cd Scripts
pythonNotebooks="../pythonNotebooks.txt"
langFile=`./get_last_output.sh "languages"`
grep -E "^nb_[0-9]+\.ipynb\, PYTHON\, " $langFile | cut -d',' -f1 > $pythonNotebooks
cd ..
rm Output
ln -s $outputSOA Output
cd Scripts
sccNotebooks="sccNotebooks.txt"
cloneFreqSCC=`./get_last_output.sh "cloneFrequency"`
connectionsSCC=`./get_last_output.sh "connections"`
sed -n "2,$ p" $cloneFreqSCC | cut -d',' -f1 > $sccNotebooks
grep -vFf $sccNotebooks $pythonNotebooks | while read notebook; do
	echo "$notebook, 0, 0, 0, 0, 0, 0, 0" >> $cloneFreqSCC
	echo "$notebook, 0, 0.0000, 0, 0.0000" >> $connectionsSCC
done
rm $pythonNotebooks
rm $sccNotebooks


# NEAR-MISS-CLONES:

# Postprocessing for SourcererCC result
./clone_analysis_scc.sh > ../Output/output_clone_analysis.txt

#Statistics for SccOutputAnanlyzer results
./create_sym_links_scc.sh
Rscript statistics_paperIII_scc.R > ../Output/output_statistics.txt
# Pack plots
./reduce_large_images.sh
cd ../Output
tar -czf plots.tgz hist_clone_frequency*eps log_hist_*eps cells*jpg *Inter_*jpg
cd ..
rm Output


# CMW CLONES AND STATISTICS FOR THE CORPUS:

# Postprocessing of NotebookAnalyzer result
ln -s $outputNBA Output
cd Scripts
./language_analysis.sh > ../Output/output_language_analysis.txt
./language_inconsistencies.sh > ../Output/output_language_inconsistencies.txt
./clone_analysis_nba.sh > ../Output/output_clone_analysis.txt
./print_most_common_snippets.sh 100 0 1 > ../Output/top100clones_min0.txt
./print_most_common_snippets.sh 100 4 1 > ../Output/top100clones_min4loc.txt
./list_duplicated_notebooks.sh	# takes 2 days on fxpl-stat

# Statistics for NotebookAnalyzer results
./create_sym_links_nba.sh
./get_notebook_sizes.sh
Rscript statistics_paperIII_nba.R > ../Output/output_statistics.txt
# Pack plots
./reduce_large_images.sh
cd ../Output
tar -czf plots.tgz hist_clone_frequency*eps lang*eps log_hist_*eps cells*jpg *Inter_*jpg
cd ..
rm Output

